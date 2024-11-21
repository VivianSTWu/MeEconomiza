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
