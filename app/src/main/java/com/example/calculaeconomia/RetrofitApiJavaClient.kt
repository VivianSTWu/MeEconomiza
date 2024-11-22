package com.example.calculaeconomia

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Certifique-se de adicionar o cliente configurado aqui
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


/*object ApiClient {

    // URL base da sua API
    private const val BASE_URL = "http://10.0.2.2:8080/"

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
}*/
