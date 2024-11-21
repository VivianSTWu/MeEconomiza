package com.example.calculaeconomia.network

import com.example.calculaeconomia.models.WeatherbitResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherbitApiService {

    @GET("history/daily")
    suspend fun getHistoricalData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("key") apiKey: String
    ): Response<WeatherbitResponse>
}
