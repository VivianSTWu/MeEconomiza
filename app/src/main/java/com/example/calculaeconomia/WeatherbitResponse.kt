package com.example.calculaeconomia.models

data class WeatherbitResponse(
    val data: List<WeatherData>?,
    val count: Int?
)

data class WeatherData(
    val wind_spd: Double?, // Velocidade do vento (m/s)
    val max_uv: Double?    // Índice UV máximo diário
)
