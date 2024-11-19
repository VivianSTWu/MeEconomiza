data class WeatherResponse(
    val main: Main,
    val wind: Wind,
    val current: Current? // Tornando 'current' opcional para evitar o NullPointerException
)

data class Main(
    val temp: Double
)

data class Wind(
    val speed: Double
)

data class Current(
    val uvi: Double // Índice UV
)

data class UvResponse(
    val version: String,
    val user: String,
    val dateGenerated: String,
    val status: String,
    val data: List<UvData>
)

data class UvData(
    val parameter: String,
    val coordinates: List<UvCoordinates>
)

data class UvCoordinates(
    val lat: Double,
    val lon: Double,
    val dates: List<UvDate>
)

data class UvDate(
    val date: String,
    val value: Double
)

