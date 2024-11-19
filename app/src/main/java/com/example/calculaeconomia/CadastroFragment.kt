package com.example.calculaeconomia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calculaeconomia.databinding.FragmentCadastroBinding
import com.example.calculaeconomia.databinding.FragmentFormularioBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class CadastroFragment : Fragment(){
    private var _binding: FragmentCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializa o View Binding
        _binding = FragmentCadastroBinding.inflate(inflater, container, false)

        // Esconde o BottomNavigationView
        (activity as? MainActivity)?.binding?.bottomNavigation?.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener{
            val name = binding.textInputEditNome.text.toString()
            val email = binding.textInputEditEmail.text.toString()

            // Verifica se os campos estão preenchidos
            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else {
                findNavController().navigate(R.id.action_cadastroFragment_to_formularioFragment)
            }
        }
    }


    /*QUANDO API JAVA ESTIVER PRONTA

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.button.setOnClickListener{
            val name = binding.textInputEditNome.text.toString()
            val email = binding.textInputEditEmail.text.toString()

            // Verifica se os campos estão preenchidos
            if (name.isBlank() || email.isBlank()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendDataToApi(name, email)
        }
    }

    private fun sendDataToApi(name: String, email: String) {
        val apiService = RetrofitInstance.api // Substitua pelo seu RetrofitInstance
        val call = apiService.sendUserData(name, email) // Método hipotético da sua API

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    // Sucesso ao enviar dados
                    Toast.makeText(requireContext(), "Dados enviados com sucesso!", Toast.LENGTH_SHORT).show()
                    // Navega para o próximo fragmento
                    findNavController().navigate(R.id.action_formularioFragment_to_locationFragment)
                } else {
                    // Tratar erros da API
                    Toast.makeText(requireContext(), "Erro ao enviar os dados", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Tratar falhas de comunicação
                Toast.makeText(requireContext(), "Falha ao se comunicar com a API", Toast.LENGTH_SHORT).show()
            }
        })
    }*/

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}