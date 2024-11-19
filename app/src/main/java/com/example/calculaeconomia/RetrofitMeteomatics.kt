package com.example.calculaeconomia

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Definindo o usuário e senha para autenticação
private const val USERNAME = "fiap_wu_vivian"
private const val PASSWORD = "b73AK4Tl6k"

// Configuração do cliente OkHttp com autenticação básica
private val client: OkHttpClient by lazy {
    val credentials = Credentials.basic(USERNAME, PASSWORD)
    OkHttpClient.Builder().addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", credentials)
            .build()
        chain.proceed(request)
    }.build()
}

// Instância do Retrofit configurada para a API Meteomatics
object RetrofitMeteomatics {
    private const val BASE_URL = "https://api.meteomatics.com/"

    val api: MeteomaticsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Cliente com autenticação básica
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeteomaticsApiService::class.java)
    }
}
