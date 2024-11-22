package com.example.calculaeconomia

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calculaeconomia.databinding.FragmentCadastroBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroFragment : Fragment() {
    private var _binding: FragmentCadastroBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCadastroBinding.inflate(inflater, container, false)

        // Esconde o BottomNavigationView
        (activity as? MainActivity)?.binding?.bottomNavigation?.visibility = View.GONE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CadastroFragment", "onViewCreated chamado")

        if (binding == null) {
            Log.e("CadastroFragment", "Binding não inicializado corretamente")
        }


        binding.button.setOnClickListener {
            Log.d("CadastroFragment", "Botão de envio clicado")

            val nome = binding.textInputEditNome.text.toString()
            val email = binding.textInputEditEmail.text.toString()

            Log.d("CadastroFragment", "Nome: $nome, Email: $email") // Verificar valores capturados


            if (nome.isBlank() || email.isBlank()) {
                Log.w("CadastroFragment", "Campos obrigatórios não preenchidos")
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Chamada para enviar dados
            enviarDados(nome, email)
        }
    }

    private fun enviarDados(nome: String, email: String) {
        Log.d("CadastroFragment", "Método enviarDados chamado com nome=$nome, email=$email")

        val usuario = Usuario(nome, email)

        // Usando Retrofit com enqueue para chamada assíncrona
        ApiClient.api.cadastrarUsuario(usuario).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("API", "Usuário cadastrado com sucesso!")
                    Toast.makeText(requireContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.formularioFragment)
                } else {
                    // Log detalhado do erro
                    Log.e("API", "Erro ao cadastrar: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Erro no cadastro: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("API", "Erro de conexão: ${t.message}")
                Toast.makeText(requireContext(), "Falha na comunicação com a API", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.binding?.bottomNavigation?.visibility = View.VISIBLE
        _binding = null
    }
}
