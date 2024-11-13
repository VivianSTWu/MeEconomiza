package com.example.calculaeconomia

data class WeatherResponse(
    val current: CurrentWeather
)

data class CurrentWeather(
    val uvi: Double,         // índice UV, útil para estimar a incidência solar
    val wind_speed: Double   // velocidade do vento em metros por segundo
)
