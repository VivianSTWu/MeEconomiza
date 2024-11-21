package com.example.calculaeconomia

import retrofit2.http.GET

interface ApiService {
    @GET("enderecos")
    suspend fun getEnderecos(): List<Endereco>

    @GET("energia-solar")
    suspend fun getEnergiaSolar(): List<EnergiaSolar>

    @GET("energia-eolica")
    suspend fun getEnergiaEolica(): List<EnergiaEolica>
}
