package com.example.calculaeconomia

import WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// URL base para a API OpenWeather
private const val BASE_URL = "https://api.openweathermap.org/"

// Configurar Retrofit para a API OpenWeather
object RetrofitInstance {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}
