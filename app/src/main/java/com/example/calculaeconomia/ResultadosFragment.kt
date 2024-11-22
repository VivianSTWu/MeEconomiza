/*
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
import androidx.recyclerview.widget.LinearLayoutManager
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
        */
/*private const val API_KEY = "4b4188bbb0e10c9709e02e6cc92b3c4c"
        private const val BASE_URL = "https://api.openweathermap.org/"*//*

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

        lifecycleScope.launch {
            val cardInfoList = fetchData(ApiClient.apiService)
            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = CardAdapter(cardInfoList)
            }
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

    private suspend fun fetchData(apiService: ApiService): List<CardInfo> {
        return try {
            val enderecos = apiService.getEnderecos()
            val energiaSolar = apiService.getEnergiaSolar()
            val energiaEolica = apiService.getEnergiaEolica()

            enderecos.map { endereco ->
                val solar = energiaSolar.find { it.id == endereco.id }?.energiaEstimadaGerada ?: 0.0
                val eolica = energiaEolica.find { it.id == endereco.id }?.energiaEstimadaGerada ?: 0.0

                CardInfo(
                    nomeEndereco = endereco.nome,
                    energiaSolarGerada = solar,
                    energiaEolicaGerada = eolica,
                    economia = endereco.economia
                )
            }
        } catch (e: Exception) {
            Log.e("ErroAPI", "Falha ao carregar dados: ${e.message}")
            emptyList()
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
*/

package com.example.calculaeconomia

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculaeconomia.databinding.FragmentResultadosBinding
import kotlinx.coroutines.launch
import kotlin.math.pow

class ResultadosFragment : Fragment() {

    private var _binding: FragmentResultadosBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultadosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mockando os dados
        val cardInfoList = mockData()

        // Verificando o conteúdo dos dados mockados
        Log.d("ResultadosFragment", "Dados mockados: ${cardInfoList.joinToString()}")

        // Configurando o RecyclerView
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CardAdapter(cardInfoList)
        }

        // Simulando o cálculo de energia solar e eólica com os dados mockados
        cardInfoList.forEach { cardInfo ->
            Log.d("ResultadosFragment", "Nome: ${cardInfo.nomeEndereco}, " +
                    "Energia Solar: ${cardInfo.energiaSolarGerada}, Energia Eólica: ${cardInfo.energiaEolicaGerada}")
        }

        // Calculando a energia solar e eólica para o primeiro item da lista
        if (cardInfoList.isNotEmpty()) {
            val firstCard = cardInfoList[0]
            estimateSolarEnergy(firstCard.energiaSolarGerada)
            estimateWindEnergy(firstCard.energiaEolicaGerada)
        }
    }

    private fun estimateSolarEnergy(energiaSolar: Double) {
        // Verificando valor de energia solar
        Log.d("ResultadosFragment", "Estimando energia solar para valor: $energiaSolar kWh")
        if (energiaSolar > 0) {
            val energiaSolarEstimativa = energiaSolar * 30 // Estimativa para o mês
            Log.d("ResultadosFragment", "Energia Solar Estimada: $energiaSolarEstimativa kWh/mês")
            binding.txtSolarEnergy.text = "Energia Solar Estimada: ${"%.2f".format(energiaSolarEstimativa)} kWh/mês"
        } else {
            Log.e("ResultadosFragment", "Valor de energia solar inválido ou nulo.")
            binding.txtSolarEnergy.text = "Energia Solar Estimada: Dados indisponíveis"
        }
    }

    private fun estimateWindEnergy(energiaEolica: Double) {
        // Verificando valor de energia eólica
        Log.d("ResultadosFragment", "Estimando energia eólica para valor: $energiaEolica kWh")
        if (energiaEolica > 0) {
            val energiaEolicaEstimativa = energiaEolica * 30 // Estimativa para o mês
            Log.d("ResultadosFragment", "Energia Eólica Estimada: $energiaEolicaEstimativa kWh/mês")
            binding.txtWindEnergy.text = "Energia Eólica Estimada: ${"%.2f".format(energiaEolicaEstimativa)} kWh/mês"
        } else {
            Log.e("ResultadosFragment", "Valor de energia eólica inválido ou nulo.")
            binding.txtWindEnergy.text = "Energia Eólica Estimada: Dados indisponíveis"
        }
    }

    // Função mockData para fornecer dados simulados
    fun mockData(): List<CardInfo> {
        // Dados mockados para teste
        val enderecos = listOf(
            Endereco(1, "Residencial", "Casa 1", "12345-678", 0.5, 200.0, 50.0),
            Endereco(2, "Residencial", "Casa 2", "23456-789", 0.7, 150.0, 60.0),
            Endereco(3, "Comercial", "Empresa X", "34567-890", 0.8, 300.0, 100.0)
        )

        val energiaSolar = listOf(
            EnergiaSolar(1, 25.0, 5.0, 1000.0),
            EnergiaSolar(2, 30.0, 5.5, 1200.0),
            EnergiaSolar(3, 50.0, 6.0, 2000.0)
        )

        val energiaEolica = listOf(
            EnergiaEolica(1, 1.5, 20.0, 10.0, 1500.0),
            EnergiaEolica(2, 2.0, 25.0, 12.0, 1800.0),
            EnergiaEolica(3, 3.0, 30.0, 15.0, 3000.0)
        )

        // Mapeando os dados para a classe CardInfo
        return enderecos.map { endereco ->
            val solar = energiaSolar.find { it.id == endereco.id }?.energiaEstimadaGerada ?: 0.0
            val eolica = energiaEolica.find { it.id == endereco.id }?.energiaEstimadaGerada ?: 0.0

            CardInfo(
                nomeEndereco = endereco.nome,
                energiaSolarGerada = solar,
                energiaEolicaGerada = eolica,
                economia = endereco.economia
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

