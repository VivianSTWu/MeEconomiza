package com.example.calculaeconomia

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calculaeconomia.databinding.FragmentFormularioBinding
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FormularioFragment : Fragment() {
    private var _binding: FragmentFormularioBinding? = null
    private val binding get() = _binding!!

    // Retrofit API e OkHttpClient com interceptor
    private val retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }


        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.distancematrix.ai/")  // Substitua pela URL real da API
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa Retrofit se necessário
        // O Retrofit está sendo criado na propriedade `retrofit` no trecho acima
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFormularioBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = "zMHrHyXl26wuNKPuCEeHDFJ2vKG2N6KpgY32IXNj97ERU7wjLH8mUhi6K6toCwBn" // Substitua pela sua chave da API

        // Configura o clique do botão
        binding.buttonSend.setOnClickListener {
            val cep = binding.TextInputEditCep.text.toString().trim()

            // Valida se o CEP foi inserido
            if (cep.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, insira um CEP válido", Toast.LENGTH_SHORT).show()
            } else {
                // Chama a função para obter as coordenadas
                getCoordinatesFromCep(cep, apiKey)
            }
        }
    }

    /*private fun getCoordinatesFromCep(cep: String, apiKey: String) {
        val call = RetrofitDistanceMatrix.api.getGeocode(address = cep, apiKey = apiKey)
        call.enqueue(object : Callback<GeocodeResponse> {
            override fun onResponse(
                call: Call<GeocodeResponse>,
                response: Response<GeocodeResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { geocodeResponse ->
                        if (geocodeResponse.status == "OK" && geocodeResponse.results != null && geocodeResponse.results.isNotEmpty()) {
                            val location = geocodeResponse.results.firstOrNull()?.geometry?.location
                            location?.let {
                                // Exibe as coordenadas ou navega para outra página
                                Toast.makeText(
                                    requireContext(),
                                    "Latitude: ${it.lat}, Longitude: ${it.lng}",
                                    Toast.LENGTH_LONG
                                ).show()

                                val bundle = Bundle().apply {
                                    putFloat("latitude", it.lat.toFloat())
                                    putFloat("longitude", it.lng.toFloat())
                                }

                                // Navegar para o próximo fragmento usando o Bundle
                                findNavController().navigate(R.id.resultadosFragment, bundle)
                            } ?: run {
                                Toast.makeText(
                                    requireContext(),
                                    "Nenhum resultado encontrado para o CEP.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Log.e("FormularioFragment", "Erro na resposta da API ou resultados vazios: ${geocodeResponse.status}")
                            Toast.makeText(
                                requireContext(),
                                "Erro na API: ${geocodeResponse.status} ou sem resultados",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Log.e("FormularioFragment", "Erro na resposta da API: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "Erro na resposta: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<GeocodeResponse>, t: Throwable) {
                Log.e("FormularioFragment", "Falha na requisição: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Falha na requisição: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }*/

    private fun getCoordinatesFromCep(cep: String, apiKey: String) {
        val call = RetrofitDistanceMatrix.api.getGeocode(address = cep, apiKey = apiKey)
        call.enqueue(object : Callback<GeocodeResponse> {
            override fun onResponse(
                call: Call<GeocodeResponse>,
                response: Response<GeocodeResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { geocodeResponse ->
                        Log.d("FormularioFragment", "Resposta completa: ${geocodeResponse}")

                        if (geocodeResponse.status == "OK" && !geocodeResponse.result.isNullOrEmpty()) {
                            // Prioriza o resultado com localização mais precisa
                            val bestResult = geocodeResponse.result.firstOrNull {
                                it.geometry.location_type == "ROOFTOP"
                            } ?: geocodeResponse.result.firstOrNull()

                            bestResult?.geometry?.location?.let { location ->
                                // Exibe as coordenadas ou navega para outra página
                                Toast.makeText(
                                    requireContext(),
                                    "Latitude: ${location.lat}, Longitude: ${location.lng}",
                                    Toast.LENGTH_LONG
                                ).show()

                                val bundle = Bundle().apply {
                                    putFloat("latitude", location.lat.toFloat())
                                    putFloat("longitude", location.lng.toFloat())
                                }

                                // Navegar para o próximo fragmento usando o Bundle
                                findNavController().navigate(R.id.resultadosFragment, bundle)
                            } ?: run {
                                Toast.makeText(
                                    requireContext(),
                                    "Nenhum resultado encontrado para o CEP.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Log.e("FormularioFragment", "Erro na resposta da API ou resultados vazios: ${geocodeResponse.status}")
                            Toast.makeText(
                                requireContext(),
                                "Erro na API: ${geocodeResponse.status} ou sem resultados",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Log.e("FormularioFragment", "Erro na resposta da API: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "Erro na resposta: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<GeocodeResponse>, t: Throwable) {
                Log.e("FormularioFragment", "Falha na requisição: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Falha na requisição: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
