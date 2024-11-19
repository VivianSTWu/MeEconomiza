package com.example.calculaeconomia

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface DistanceMatrixApiService {
    @GET("maps/api/geocode/json")
    fun getGeocode(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): Call<GeocodeResponse>
}
