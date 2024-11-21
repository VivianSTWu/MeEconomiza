package com.example.calculaeconomia

data class CardInfo(
    val nomeEndereco: String,
    val energiaSolarGerada: Double,
    val energiaEolicaGerada: Double,
    val economia: Double
)

data class Usuario(
    val nome: String,
    val email: String
)

data class Endereco(
    val id: Int,
    val tipoResidencial: String,
    val nome: String,
    val cep: String,
    val tarifa: Double,
    val gastoMensal: Double,
    val economia: Double
)

data class EnergiaEolica(
    val id: Int,
    val potenciaNominal: Double,
    val alturaTorre: Double,
    val diametroRotor: Double,
    val energiaEstimadaGerada: Double
)

data class EnergiaSolar(
    val id: Int,
    val areaPlaca: Double,
    val irradiacaoSolar: Double,
    val energiaEstimadaGerada: Double
)
