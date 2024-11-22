package com.example.calculaeconomia

import android.content.Context
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

        val apiKey = "zMHrHyXl26wuNKPuCEeHDFJ2vKG2N6KpgY32IXNj97ERU7wjLH8mUhi6K6toCwBn"

        // Configura o clique do botão
        binding.btnCalcular.setOnClickListener {
            val cep = binding.TextInputEditCep.text.toString().trim()

            // Validação do CEP
            if (cep.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor, insira um CEP válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtém coordenadas ou chama diretamente o POST, dependendo da lógica desejada
            getCoordinatesFromCep(cep, apiKey)

            // Realiza o POST
            enviarPost()
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
                    Log.d("FormularioFragment", "Resposta do servidor: ${response.body()}")
                    response.body()?.let { geocodeResponse ->
                        Log.d("FormularioFragment", "Resposta completa: ${geocodeResponse}")

                        if (geocodeResponse.status == "OK" && !geocodeResponse.result.isNullOrEmpty()) {
                            // Prioriza o resultado com localização mais precisa
                            val bestResult = geocodeResponse.result.firstOrNull {
                                it.geometry.location_type == "ROOFTOP"
                            } ?: geocodeResponse.result.firstOrNull()

                            bestResult?.geometry?.location?.let { location ->

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
                            Log.e("FormularioFragment", "Erro: ${response.errorBody()?.string()}")
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

    private fun enviarPost() {
        val sharedPreferences = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val usuarioId = sharedPreferences.getInt("user_id", -1)

        val nome = binding.TextInputEditApelido.text.toString()
        val cep = binding.TextInputEditCep.text.toString()
        val tarifa = binding.TextInputEditTarifa.text.toString().toDouble()
        val gastoMensal = binding.TextInputEditGasto.text.toString().toDouble()

        val endereco = Endereco(null,
            tipoResidencial = "Residencial",
            nome = nome,
            cep = cep,
            tarifa = tarifa,
            gastoMensal = gastoMensal,
            economia = 0.0,
            fk_usuario = usuarioId
        )

        Log.d("enviarPost", "Corpo do POST: ${endereco}")

        val apiService = ApiClient.api
        val call = apiService.cadastrarEndereco(endereco)

        call.enqueue(object : Callback<Endereco> {
            override fun onResponse(call: Call<Endereco>, response: Response<Endereco>) {
                if (response.isSuccessful) {
                    val enderecoCriado = response.body()
                    val enderecoId = enderecoCriado?.id

                    if (enderecoId != null) {
                        sharedPreferences.edit().putInt("endereco_id", enderecoId).apply()
                        Toast.makeText(context, "Endereço cadastrado! ID: $enderecoId", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("enviarPost", "Resposta sem ID de endereço.")
                    }
                } else {
                    Log.e("enviarPost", "Erro no cadastro: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Erro ao cadastrar o endereço.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Endereco>, t: Throwable) {
                Log.e("enviarPost", "Erro na conexão: ${t.message}")
                Toast.makeText(context, "Falha na comunicação com a API.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
