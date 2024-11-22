package com.example.calculaeconomia

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // USUÁRIOS
    @POST("usuario")
    fun cadastrarUsuario(@Body user: Usuario): Call<Void>  // Alterado para retornar Call<Void>

    // ENDEREÇOS
    @GET("endereco")
    fun getEnderecos(): Call<List<Endereco>>  // Alterado para retornar Call<List<Endereco>>

    // ENERGIA SOLAR
    @GET("energiaSolar")
    fun getEnergiaSolar(): Call<List<EnergiaSolar>>  // Alterado para retornar Call<List<EnergiaSolar>>

    // ENERGIA EÓLICA
    @GET("energiaEolica")
    fun getEnergiaEolica(): Call<List<EnergiaEolica>>  // Alterado para retornar Call<List<EnergiaEolica>>
}

