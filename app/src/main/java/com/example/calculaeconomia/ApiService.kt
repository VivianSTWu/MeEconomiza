package com.example.calculaeconomia

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // USUÁRIOS
    @POST("usuario")
    fun cadastrarUsuario(@Body user: Usuario): Call<Usuario>  // Alterado para retornar Call<Void>

    // ENDEREÇOS
    @GET("endereco")
    fun getEnderecos(): Call<List<Endereco>>  // Alterado para retornar Call<List<Endereco>>

    @GET("endereco/{id}")
    fun getEnderecoById(@Path("id") id: Int): Call<Endereco>

    @POST("endereco")
    fun cadastrarEndereco(@Body endereco: Endereco): Call<Endereco>

    @PATCH("endereco/{id}")
    fun updateEndereco(@Path("id") id: Int, @Body endereco: Endereco): Call<Endereco>

    // ENERGIA SOLAR
    @GET("energiaSolar")
    fun getEnergiaSolar(): Call<List<EnergiaSolar>>

    @POST("energiaSolar")
    fun enviarEnergiaSolar(@Body energiaSolar: EnergiaSolar): Call<EnergiaSolar>

    // ENERGIA EÓLICA
    @GET("energiaEolica")
    fun getEnergiaEolica(): Call<List<EnergiaEolica>>  // Alterado para retornar Call<List<EnergiaEolica>>

    @POST("energiaEolica")
    fun enviarEnergiaEolica(@Body energiaEolica: EnergiaEolica): Call<EnergiaEolica>
}

