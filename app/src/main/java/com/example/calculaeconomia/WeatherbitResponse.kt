package com.example.calculaeconomia

data class WeatherbitResponse(
    val data: List<DailyData>
)

data class DailyData(
    val datetime: String,
    val uv: Double,
    val wind_spd: Double
)

