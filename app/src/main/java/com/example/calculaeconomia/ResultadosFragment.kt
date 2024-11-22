package com.example.calculaeconomia

import UvResponse
import WeatherApiService
import WeatherResponse
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculaeconomia.databinding.FragmentResultadosBinding
import com.example.calculaeconomia.network.WeatherbitRetrofit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

class ResultadosFragment : Fragment() {

    private var _binding: FragmentResultadosBinding? = null
    private val binding get() = _binding!!

    private var energiaSolarGerada: Double? = null
    private var energiaEolicaGerada: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
        val enderecoId = sharedPreferences.getInt("endereco_id", -1)
        val usuarioId = sharedPreferences.getInt("user_id", -1)

        // Logando o ID do endereço e do usuário
        Log.d("ResultadosFragment", "ID do Endereço: $enderecoId")
        Log.d("ResultadosFragment", "ID do Usuário: $usuarioId")

        // Recuperar latitude e longitude passados como argumento
        val latitude = arguments?.getFloat("latitude")?.toDouble()
        val longitude = arguments?.getFloat("longitude")?.toDouble()

        // Verificar se as coordenadas foram passadas corretamente
        if (latitude != null && longitude != null) {
            lifecycleScope.launch {
                val (avgWindSpeed, avgUVIndex) = fetchWeatherData(latitude, longitude)
                if (avgWindSpeed != null && avgUVIndex != null) {
                    estimateSolarEnergy(avgUVIndex, enderecoId)
                    estimateWindEnergy(avgWindSpeed, enderecoId)

                    if (energiaSolarGerada != null && energiaEolicaGerada != null) {
                        calcularEconomia(enderecoId, energiaSolarGerada!!, energiaEolicaGerada!!)
                    } else {
                        Log.e("ResultadosFragment", "Valores de energia não disponíveis para cálculo da economia.")
                    }
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

    private fun estimateSolarEnergy(avgUVIndex: Double?, enderecoId: Int) {
        if (avgUVIndex != null) {
            val irradiance = avgUVIndex * 25.0
            val areaOfPanels = 10.0
            val panelEfficiency = 0.18

            val solarEnergyGenerated = (areaOfPanels * panelEfficiency * irradiance * 5) / 1000
            val solarEnergyGeneratedMonth = solarEnergyGenerated * 30

            // Armazena o valor calculado
            energiaSolarGerada = solarEnergyGeneratedMonth

            binding.txtUvIndex.text = "Índice UV Médio:\n${"%.2f".format(avgUVIndex)}"
            binding.txtSolarEnergy.text = "Energia Solar Estimada:\n${"%.2f".format(solarEnergyGeneratedMonth)} kWh/mês"

            Log.d("ResultadosFragment", "Energia Solar Estimada: $solarEnergyGeneratedMonth kWh/mês")

            val energiaSolar = EnergiaSolar(
                null,
                areaPlaca = areaOfPanels,
                irradiacaoSolar = avgUVIndex,
                energiaEstimadaGerada = solarEnergyGeneratedMonth,
                fk_endereco = enderecoId
            )
            sendEnergiaSolar(energiaSolar)
        } else {
            Log.e("ResultadosFragment", "Índice UV médio nulo, cálculo não realizado.")
            binding.txtSolarEnergy.text = "Energia Solar Estimada:\nDados indisponíveis"
        }
    }


    private fun estimateWindEnergy(avgWindSpeed: Double?, enderecoId: Int) {
        if (avgWindSpeed != null) {
            val turbineArea = 10.0
            val airDensity = 1.225

            val windEnergyGenerated = (0.5 * airDensity * turbineArea * avgWindSpeed.pow(3) * 0.35) / 1000
            val windEnergyGeneratedMonth = windEnergyGenerated * 30

            // Armazena o valor calculado
            energiaEolicaGerada = windEnergyGeneratedMonth

            binding.txtWindSpeed.text = "Média velocidade vento:\n${"%.2f".format(avgWindSpeed)}"
            binding.txtWindEnergy.text = "Energia Eólica Estimada:\n${"%.2f".format(windEnergyGeneratedMonth)} kWh/mês"

            Log.d("ResultadosFragment", "Energia Eólica Estimada: $windEnergyGeneratedMonth kWh/mês")

            val energiaEolica = EnergiaEolica(
                null,
                potenciaNominal = 500.0,
                alturaTorre = 15.0,
                diametroRotor = 15.0,
                energiaEstimadaGerada = windEnergyGeneratedMonth,
                fk_endereco = enderecoId
            )
            sendEnergiaEolica(energiaEolica)
        } else {
            Log.e("ResultadosFragment", "Velocidade do vento média nula, cálculo não realizado.")
            binding.txtWindEnergy.text = "Energia Eólica Estimada:\nDados indisponíveis"
        }
    }


    private fun sendEnergiaSolar(energiaSolar: EnergiaSolar) {
        ApiClient.api.enviarEnergiaSolar(energiaSolar).enqueue(object : Callback<EnergiaSolar> {
            override fun onResponse(call: Call<EnergiaSolar>, response: Response<EnergiaSolar>) {
                if (response.isSuccessful) {
                    Log.d("ResultadosFragment", "Energia solar enviada com sucesso.")
                } else {
                    Log.e("ResultadosFragment", "Erro ao enviar energia solar.")
                }
            }

            override fun onFailure(call: Call<EnergiaSolar>, t: Throwable) {
                Log.e("ResultadosFragment", "Falha na requisição de energia solar: ${t.message}")
            }
        })
    }

    private fun sendEnergiaEolica(energiaEolica: EnergiaEolica) {
        ApiClient.api.enviarEnergiaEolica(energiaEolica).enqueue(object : Callback<EnergiaEolica> {
            override fun onResponse(call: Call<EnergiaEolica>, response: Response<EnergiaEolica>) {
                if (response.isSuccessful) {
                    Log.d("ResultadosFragment", "Energia eólica enviada com sucesso.")
                } else {
                    Log.e("ResultadosFragment", "Erro ao enviar energia eólica.")
                }
            }

            override fun onFailure(call: Call<EnergiaEolica>, t: Throwable) {
                Log.e("ResultadosFragment", "Falha na requisição de energia eólica: ${t.message}")
            }
        })
    }

    private fun updateEconomia(enderecoId: Int, economia: Double) {
        // Primeiro, fazer um GET para buscar os dados atuais do endereço
        Log.d("ResultadosFragment", "Buscando dados do endereço $enderecoId para atualizar economia.")
        ApiClient.api.getEnderecoById(enderecoId).enqueue(object : Callback<Endereco> {
            override fun onResponse(call: Call<Endereco>, response: Response<Endereco>) {
                if (response.isSuccessful) {
                    val enderecoAtual = response.body()
                    if (enderecoAtual != null) {
                        // Manter os dados atuais do endereço e atualizar somente a economia
                        val enderecoAtualizado = enderecoAtual.copy(economia = economia)

                        Log.d("ResultadosFragment", "Atualizando economia: $economia para o endereço $enderecoId")
                        // Enviar a atualização com os dados existentes e a nova economia
                        ApiClient.api.updateEndereco(enderecoAtualizado.id!!, enderecoAtualizado).enqueue(object : Callback<Endereco> {
                            override fun onResponse(call: Call<Endereco>, response: Response<Endereco>) {
                                if (response.isSuccessful) {
                                    Log.d("ResultadosFragment", "Economia atualizada com sucesso para o endereço $enderecoId")
                                } else {
                                    Log.e("ResultadosFragment", "Erro ao atualizar economia: ${response.code()}")
                                }
                            }

                            override fun onFailure(call: Call<Endereco>, t: Throwable) {
                                Log.e("ResultadosFragment", "Falha na requisição de atualização de economia: ${t.message}")
                            }
                        })
                    } else {
                        Log.e("ResultadosFragment", "Endereço não encontrado no banco de dados.")
                    }
                } else {
                    Log.e("ResultadosFragment", "Erro ao buscar endereço: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Endereco>, t: Throwable) {
                Log.e("ResultadosFragment", "Falha na requisição de busca de endereço: ${t.message}")
            }
        })
    }

    private fun calcularEconomia(enderecoId: Int, energiaSolar: Double, energiaEolica: Double) {
        // Primeiro, buscar a tarifa do banco de dados usando o ID do endereço
        ApiClient.api.getEnderecoById(enderecoId).enqueue(object : Callback<Endereco> {
            override fun onResponse(call: Call<Endereco>, response: Response<Endereco>) {
                if (response.isSuccessful) {
                    val endereco = response.body()
                    if (endereco != null) {
                        // Obter a tarifa do endereço
                        val tarifa = endereco.tarifa ?: 0.0

                        // Calcular economia
                        val economia = (energiaSolar + energiaEolica) * tarifa

                        Log.d("calcularEconomia", "Tarifa: $tarifa, Energia Solar: $energiaSolar, Energia Eólica: $energiaEolica, Economia: $economia")

                        // Atualizar a economia no banco de dados
                        updateEconomia(enderecoId, economia)
                    } else {
                        Log.e("calcularEconomia", "Endereço não encontrado no banco de dados.")
                    }
                } else {
                    Log.e("calcularEconomia", "Erro ao buscar endereço: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Endereco>, t: Throwable) {
                Log.e("calcularEconomia", "Falha na requisição para buscar endereço: ${t.message}")
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
