package com.example.calculaeconomia

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitDistanceMatrix {
    private const val BASE_URL = "https://api.distancematrix.ai/"

    val api: DistanceMatrixApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DistanceMatrixApiService::class.java)
    }
}
