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
import com.example.calculaeconomia.databinding.FragmentResultadosBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.pow

class ResultadosFragment : Fragment() {

    private var _binding: FragmentResultadosBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val API_KEY = "4b4188bbb0e10c9709e02e6cc92b3c4c"
        private const val BASE_URL = "https://api.openweathermap.org/"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResultadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (checkLocationPermission()) {
            getUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    Log.d("LocationFragment", "Localização: Latitude $latitude, Longitude $longitude")
                    fetchWeatherData(latitude, longitude) // Atualiza os dados do clima
                    fetchUvIndex(latitude, longitude) // Atualiza o índice UV
                } ?: run {
                    Log.d("LocationFragment", "Não foi possível obter a localização")
                }
            }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
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
                        val uvIndex = weatherResponse.current?.uvi ?: 0.0 // Se 'current' for nulo, define 'uvIndex' como 0.0

                        Log.d("LocationFragment", "Temperatura: $temperature°C, Velocidade do vento: $windSpeed m/s, Índice UV: $uvIndex")

                        updateUI(temperature, windSpeed, uvIndex)
                    }
                } else {
                    Log.e("LocationFragment", "Erro na resposta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("LocationFragment", "Erro na chamada da API: ${t.message}")
            }
        })
    }

    private fun fetchUvIndex(latitude: Double, longitude: Double) {
        val datetime = "now"
        val parameters = "uv:idx"
        val location = "${latitude},${longitude}"
        val format = "json"

        val call = RetrofitMeteomatics.api.getUvIndex(datetime, parameters, location, format)
        call.enqueue(object : Callback<UvResponse> {
            override fun onResponse(call: Call<UvResponse>, response: Response<UvResponse>) {
                if (response.isSuccessful) {
                    val uvIndex = response.body()?.data?.firstOrNull()?.coordinates?.firstOrNull()?.dates?.firstOrNull()?.value ?: 0.00
                    binding.txtUvIndex.text = "Índice UV: $uvIndex"
                    estimateSolarEnergy(uvIndex)
                } else {
                    Log.e("LocationFragment", "Erro na resposta UV: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UvResponse>, t: Throwable) {
                Log.e("LocationFragment", "Erro na chamada da API UV: ${t.message}")
            }
        })
    }


    private fun estimateSolarEnergy(uvIndex: Double) {
        val irradiance = uvIndex * 10 // Estimativa simples (os valores reais dependeriam de mais dados, como a localização)

        // A quantidade de energia gerada em um painel solar depende da área e da eficiência, então vamos usar uma suposição:
        val areaOfPanels = 10.0 // Área em metros quadrados
        val panelEfficiency = 0.18 // Eficiência dos painéis solares (18%)

        // A geração de energia pode ser aproximada como:
        val solarEnergyGenerated = areaOfPanels * panelEfficiency * irradiance * 5 // 5 horas de exposição solar por dia

        binding.txtSolarEnergy.text = "Energia Solar Estimada:\n$solarEnergyGenerated kWh/dia"
    }

    private fun estimateWindEnergy(windSpeed: Double) {
        // Supondo uma microturbina com eficiência de 35%
        val turbineArea = 10.0 // Área varrida pelas lâminas da turbina em metros quadrados (exemplo)
        val airDensity = 1.225 // Densidade do ar (kg/m³ ao nível do mar)

        // Fórmula simplificada para cálculo de energia
        val windEnergyGenerated = 0.5 * airDensity * turbineArea * windSpeed.pow(3) * 0.35 // Eficiência de 35%

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
