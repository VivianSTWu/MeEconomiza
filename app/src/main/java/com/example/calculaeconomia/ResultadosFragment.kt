package com.example.calculaeconomia

import UvResponse
import WeatherApiService
import WeatherResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.calculaeconomia.databinding.FragmentResultadosBinding
import com.example.calculaeconomia.network.WeatherbitRetrofit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

class ResultadosFragment : Fragment() {

    private var _binding: FragmentResultadosBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        /*private const val API_KEY = "4b4188bbb0e10c9709e02e6cc92b3c4c"
        private const val BASE_URL = "https://api.openweathermap.org/"*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultadosBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val latitude = arguments?.getFloat("latitude")?.toDouble()
        val longitude = arguments?.getFloat("longitude")?.toDouble()

        if (latitude != null && longitude != null) {
            lifecycleScope.launch {
                val (avgWindSpeed, avgUVIndex) = fetchWeatherData(latitude, longitude)
                if (avgWindSpeed != null && avgUVIndex != null) {
                    estimateSolarEnergy(avgUVIndex)
                    estimateWindEnergy(avgWindSpeed)
                } else {
                    Log.e("ResultadosFragment", "Dados meteorológicos não disponíveis para cálculo.")
                }
            }
        } else {
            Log.e("ResultadosFragment", "Coordenadas não fornecidas.")
        }
    }


    private suspend fun fetchWeatherData(latitude: Double, longitude: Double): Pair<Double?, Double?> {
        val apiKey = "62123cdbe631436eb8f03c79958921b9"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -30) // Últimos 30 dias
        val startDate = dateFormat.format(calendar.time)

        return try {
            val response = WeatherbitRetrofit.api.getHistoricalData(
                latitude = latitude,
                longitude = longitude,
                startDate = startDate,
                endDate = endDate,
                apiKey = apiKey
            )

            if (response.isSuccessful) {
                response.body()?.let { weatherResponse ->
                    val data = weatherResponse.data
                    if (!data.isNullOrEmpty()) {
                        val avgWindSpeed = data.mapNotNull { it.wind_spd }.average()
                        val avgUVIndex = data.mapNotNull { it.max_uv }.average()
                        Pair(avgWindSpeed, avgUVIndex)
                    } else {
                        Log.e("Weatherbit", "Nenhum dado histórico encontrado.")
                        Pair(null, null)
                    }
                } ?: Pair(null, null)
            } else {
                Log.e("Weatherbit", "Erro na API: ${response.code()}")
                Pair(null, null)
            }
        } catch (e: Exception) {
            Log.e("Weatherbit", "Erro: ${e.message}")
            Pair(null, null)
        }
    }


    private fun estimateSolarEnergy(avgUVIndex: Double?) {
        if (avgUVIndex != null) {
            // O índice UV é convertido em irradiância solar estimada (em W/m²)
            val irradiance = avgUVIndex * 25.0 // Fator aproximado para conversão UV -> irradiância

            // Área das placas solares (m²) e eficiência média do painel (%)
            val areaOfPanels = 10.0 // Exemplo: 10 m² de painéis solares
            val panelEfficiency = 0.18 // Eficiência de 18%

            // Cálculo de energia solar gerada (kWh/dia)
            val solarEnergyGenerated = (areaOfPanels * panelEfficiency * irradiance * 5) / 1000
            val solarEnergyGeneratedMonth = solarEnergyGenerated * 30

            // Exibir o resultado no TextView
            binding.txtUvIndex.text = "Índice UV Médio:\n${"%.2f".format(avgUVIndex)}"
            binding.txtSolarEnergy.text = "Energia Solar Estimada:\n${"%.2f".format(solarEnergyGeneratedMonth)} kWh/mês"
        } else {
            Log.e("ResultadosFragment", "Índice UV médio nulo, cálculo não realizado.")
            binding.txtSolarEnergy.text = "Energia Solar Estimada:\nDados indisponíveis"
        }
    }

    private fun estimateWindEnergy(avgWindSpeed: Double?) {
        if (avgWindSpeed != null) {
            // Área da turbina eólica (m²) e densidade do ar (kg/m³)
            val turbineArea = 10.0 // Exemplo: turbina com área de 10 m²
            val airDensity = 1.225 // Densidade do ar padrão ao nível do mar (kg/m³)

            // Cálculo de energia eólica gerada (kWh/dia)
            val windEnergyGenerated = (0.5 * airDensity * turbineArea * avgWindSpeed.pow(3) * 0.35) / 1000
            val windEnergyGeneratedMonth = windEnergyGenerated * 30

            // Exibir o resultado no TextView
            binding.txtWindSpeed.text = "Média velocidade vento:\n${"%.2f".format(avgWindSpeed)}"
            binding.txtWindEnergy.text = "Energia Eólica Estimada:\n${"%.2f".format(windEnergyGeneratedMonth)} kWh/mês"
        } else {
            Log.e("ResultadosFragment", "Velocidade do vento média nula, cálculo não realizado.")
            binding.txtWindEnergy.text = "Energia Eólica Estimada:\nDados indisponíveis"
        }
    }



    /*private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)
        val call = api.getWeatherData(latitude, longitude, API_KEY)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        val temperature = weatherResponse.main.temp
                        val windSpeed = weatherResponse.wind.speed
                        val uvIndex = weatherResponse.current?.uvi ?: 0.0

                        Log.d(
                            "ResultadosFragment",
                            "Temperatura: $temperature°C, Velocidade do vento: $windSpeed m/s, Índice UV: $uvIndex"
                        )

                        updateUI(temperature, windSpeed, uvIndex)
                    }
                } else {
                    Log.e("ResultadosFragment", "Erro na resposta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("ResultadosFragment", "Erro na chamada da API: ${t.message}")
            }
        })
    }*/


   /* VERSÃO ANTERIOR, UTILIZANDO DADOS ATUAIS

   private fun estimateSolarEnergy(uvIndex: Double?) {
        if (uvIndex != null) {
            val irradiance = uvIndex * 10

            val areaOfPanels = 10.0
            val panelEfficiency = 0.18

            val solarEnergyGenerated = areaOfPanels * panelEfficiency * irradiance * 5

            binding.txtSolarEnergy.text = "Energia Solar Estimada:\n$solarEnergyGenerated kWh/dia"
        } else{
            Log.e("ResultadosFragment", "Índice UV nulo, cálculo não realizado.")
        }
    }

    private fun estimateWindEnergy(windSpeed: Double) {
        val turbineArea = 10.0
        val airDensity = 1.225

        val windEnergyGenerated = 0.5 * airDensity * turbineArea * windSpeed.pow(3) * 0.35

        binding.txtWindEnergy.text = "Energia Eólica Estimada:\n$windEnergyGenerated kWh/dia"
    }*/


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
