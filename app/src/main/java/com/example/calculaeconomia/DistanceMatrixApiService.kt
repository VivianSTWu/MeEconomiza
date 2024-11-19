package com.example.calculaeconomia

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class GeocodeResponse(
    val results: List<GeocodeResult>,
    val status: String
)

data class GeocodeResult(
    val formatted_address: String,
    val geometry: Geometry
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)


interface DistanceMatrixApiService {
    @GET("maps/api/geocode/json")
    fun getGeocode(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): Call<GeocodeResponse>
}
