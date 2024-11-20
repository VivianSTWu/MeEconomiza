package com.example.calculaeconomia

data class GeocodeResponse(
    val status: String,
    val result: List<Result>? // Nome do campo alterado para "result", que é o campo real da resposta.
)

data class Result(
    val address_components: List<AddressComponent>,
    val formatted_address: String,
    val geometry: Geometry,
    val place_id: String,
    val plus_code: Map<String, String>, // Ajuste o tipo de dados conforme o formato da resposta.
    val types: List<String>
)

data class AddressComponent(
    val long_name: String,
    val short_name: String,
    val types: List<String>
)

data class Geometry(
    val location: Location,
    val location_type: String,
    val viewport: Viewport
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Viewport(
    val northeast: Location,
    val southwest: Location
)
