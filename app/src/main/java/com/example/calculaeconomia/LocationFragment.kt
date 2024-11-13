package com.example.calculaeconomia

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.calculaeconomia.databinding.FragmentLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        fun newInstance(): LocationFragment {
            return LocationFragment()
        }

        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val API_KEY = "4b4188bbb0e10c9709e02e6cc92b3c4c"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.buttonGetLocation.setOnClickListener {
            if (checkLocationPermission()) {
                getUserLocation()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
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
                    binding.textViewLocation.text = "Latitude: $latitude, Longitude: $longitude"
                    fetchWeatherData(latitude, longitude)  // Chama a função com a API key
                } ?: run {
                    binding.textViewLocation.text = "Não foi possível obter a localização"
                }
            }
        } else {
            binding.textViewLocation.text = "Permissão de localização não concedida"
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val call = RetrofitInstance.api.getWeatherData(latitude, longitude, API_KEY)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        val uvi = weatherResponse.current.uvi
                        val windSpeed = weatherResponse.current.wind_speed
                        binding.textViewWeatherData.text = "Índice UV: $uvi, Velocidade do vento: $windSpeed"
                    }
                } else {
                    binding.textViewWeatherData.text = "Erro na resposta: ${response.errorBody()?.string()}"
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                binding.textViewWeatherData.text = "Erro na chamada da API: ${t.message}"
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                binding.textViewLocation.text = "Permissão de localização negada"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
