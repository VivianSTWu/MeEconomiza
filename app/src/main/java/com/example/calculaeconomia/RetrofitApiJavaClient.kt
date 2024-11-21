package com.example.calculaeconomia

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {

    // URL base da sua API
    private const val BASE_URL = "http://localhost:8080/"

    // Criação do Retrofit com OkHttp para log e Gson como conversor
    val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)  // URL base da sua API
            .client(client)  // Adicionando OkHttpClient ao Retrofit
            .addConverterFactory(GsonConverterFactory.create())  // Usando o Gson para converter a resposta da API
            .build()
    }

    // Interface que define os métodos para acessar os endpoints da API
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
