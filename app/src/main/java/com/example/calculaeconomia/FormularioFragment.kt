package com.example.calculaeconomia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calculaeconomia.databinding.FragmentFormularioBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FormularioFragment : Fragment(){
    private var _binding: FragmentFormularioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFormularioBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiKey = "SUA_API_KEY" // Substitua pela sua chave da API Distance Matrix

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

    private fun getCoordinatesFromCep(cep: String, apiKey: String) {
        val call = RetrofitDistanceMatrix.api.getGeocode(address = cep, apiKey = apiKey)
        call.enqueue(object : Callback<GeocodeResponse> {
            override fun onResponse(
                call: Call<GeocodeResponse>,
                response: Response<GeocodeResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { geocodeResponse ->
                        if (geocodeResponse.status == "OK") {
                            val location = geocodeResponse.results.firstOrNull()?.geometry?.location
                            location?.let {
                                // Exibe as coordenadas ou navega para outra página
                                Toast.makeText(
                                    requireContext(),
                                    "Latitude: ${it.lat}, Longitude: ${it.lng}",
                                    Toast.LENGTH_LONG
                                ).show()

                                val bundle = Bundle().apply {
                                    putFloat("latitude", it.lat.toFloat()) // Substitua `it.lat` pela sua variável real
                                    putFloat("longitude", it.lng.toFloat()) // Substitua `it.lng` pela sua variável real
                                }

// Navegar para o próximo fragmento usando o Bundle
                                findNavController().navigate(R.id.resultadosFragment, bundle)
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Erro na API: ${geocodeResponse.status}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Erro na resposta: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: Call<GeocodeResponse>, t: Throwable) {
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