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
    val tipoResidencial: String,
    val nome: String,
    val cep: String,
    val tarifa: Float,
    val gastoMensal: Float,
    val economia: Float
)

data class EnergiaEolica(
    val potenciaNominal: Float,
    val alturaTorre: Float,
    val diametroRotor: Float,
    val energiaEstimadaGerada: Float
)

data class EnergiaSolar(
    val areaPlaca: Float,
    val irradiacaoSolar: Float,
    val energiaEstimadaGerada: Float
)
