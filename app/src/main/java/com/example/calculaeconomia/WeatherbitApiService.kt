package com.example.calculaeconomia

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherbitApiService {
    @GET("v2.0/history/daily")
    fun getHistoricalWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("key") apiKey: String
    ): Call<WeatherbitResponse>
}
