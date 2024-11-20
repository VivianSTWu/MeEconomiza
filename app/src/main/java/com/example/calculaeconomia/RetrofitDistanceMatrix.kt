package com.example.calculaeconomia

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitDistanceMatrix {
    private const val BASE_URL = "https://api.distancematrix.ai/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: DistanceMatrixApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Certifique-se de adicionar o cliente configurado aqui
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DistanceMatrixApiService::class.java)
    }
}

