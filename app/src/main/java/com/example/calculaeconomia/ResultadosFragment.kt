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
import androidx.navigation.fragment.findNavController
import com.example.calculaeconomia.databinding.FragmentResultadosBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
        private const val API_KEY = "62123cdbe631436eb8f03c79958921b9" //Weatherbit
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

        // Recupera as coordenadas passadas da tela anterior
        val latitude = arguments?.getFloat("latitude")?.toDouble() ?: 0.0
        val longitude = arguments?.getFloat("longitude")?.toDouble() ?: 0.0

        Log.d("ResultadosFragment", "Coordenadas recebidas: Latitude $latitude, Longitude $longitude")

        // Atualiza os dados com base nas coordenadas recebidas
        fetchWeatherData(latitude, longitude)
        fetchUvIndex(latitude, longitude)
    }


    private fun fetchWeatherData(lat: Double, lon: Double) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val endDate = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = dateFormat.format(calendar.time)

        RetrofitWeatherbit.api.getHistoricalWeather(lat, lon, startDate, endDate, API_KEY)
            .enqueue(object : Callback<WeatherbitResponse> {
                override fun onResponse(
                    call: Call<WeatherbitResponse>,
                    response: Response<WeatherbitResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { weatherResponse ->
                            if (weatherResponse.data.isNotEmpty()) {
                                val uvAverage = weatherResponse.data.map { it.uv }.average()
                                val windAverage = weatherResponse.data.map { it.wind_spd }.average()

                                Toast.makeText(
                                    requireContext(),
                                    "Média UV: $uvAverage, Média Vento: $windAverage m/s",
                                    Toast.LENGTH_LONG
                                ).show()

                                val bundle = Bundle().apply {
                                    putDouble("uvAverage", uvAverage)
                                    putDouble("windAverage", windAverage)
                                }
                                findNavController().navigate(R.id.resultadosFragment, bundle)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Nenhum dado encontrado para o período.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Log.e("FormularioFragment", "Erro na resposta: ${response.errorBody()}")
                        Toast.makeText(
                            requireContext(),
                            "Erro ao obter dados meteorológicos.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<WeatherbitResponse>, t: Throwable) {
                    Log.e("FormularioFragment", "Erro na chamada da API", t)
                    Toast.makeText(
                        requireContext(),
                        "Erro de conexão com a API.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
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

    private fun fetchUvIndex(latitude: Double, longitude: Double) {
        val datetime = "now"
        val parameters = "uv:idx"
        val location = "$latitude,$longitude"
        val format = "json"

        val call = RetrofitMeteomatics.api.getUvIndex(datetime, parameters, location, format)
        call.enqueue(object : Callback<UvResponse> {
            override fun onResponse(call: Call<UvResponse>, response: Response<UvResponse>) {
                if (response.isSuccessful) {
                    val uvIndex = response.body()?.data
                        ?.firstOrNull()?.coordinates
                        ?.firstOrNull()?.dates
                        ?.firstOrNull()?.value

                    if (uvIndex != null) {
                        binding.txtUvIndex.text = "Índice UV: $uvIndex"
                        estimateSolarEnergy(uvIndex)
                    } else {
                        Log.e("ResultadosFragment", "Dados insuficientes para calcular o índice UV.")
                    }

                    binding.txtUvIndex.text = "Índice UV: $uvIndex"
                    estimateSolarEnergy(uvIndex)
                } else {
                    Log.e("ResultadosFragment", "Erro na resposta UV: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UvResponse>, t: Throwable) {
                Log.e("ResultadosFragment", "Erro na chamada da API UV: ${t.message}")
            }
        })
    }

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
    }

    private fun updateUI(temperature: Double, windSpeed: Double, uvIndex: Double) {
        binding.txtUvIndex.text = "Índice UV: $uvIndex"
        binding.txtWindSpeed.text = "Velocidade do Vento: $windSpeed m/s"
        binding.txtTemperature.text = "Temperatura: $temperature°C"
        estimateSolarEnergy(uvIndex)
        estimateWindEnergy(windSpeed)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
