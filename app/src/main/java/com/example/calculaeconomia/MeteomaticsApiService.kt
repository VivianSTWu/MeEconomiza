package com.example.calculaeconomia

import UvResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface MeteomaticsApiService {
    @GET("{validdatetime}/{parameters}/{location}/{format}")
    fun getUvIndex(
        @Path("validdatetime") datetime: String,
        @Path("parameters") parameters: String,
        @Path("location") location: String,
        @Path("format") format: String
    ): Call<UvResponse>
}

