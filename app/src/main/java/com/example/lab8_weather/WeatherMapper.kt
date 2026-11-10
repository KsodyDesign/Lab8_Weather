package com.example.lab8_weather

import java.text.SimpleDateFormat
import java.util.Locale

data class WeatherCondition(val description: String, val emoji: String)
data class ForecastDay(val date: String, val minimum: Double, val maximum: Double,
    val condition: WeatherCondition)
data class WeatherUiModel(
    val city: String,
    val temperature: Double,
    val windSpeed: Double,
    val condition: WeatherCondition,
    val updated: String,
    val days: List<ForecastDay>
)

object WeatherMapper {
    fun condition(code: Int?): WeatherCondition = when (code) {
        0 -> WeatherCondition("Ясно", "☀️")
        1 -> WeatherCondition("Малооблачно", "🌤️")
        2 -> WeatherCondition("Переменная облачность", "⛅")
        3 -> WeatherCondition("Облачно", "☁️")
        45, 48 -> WeatherCondition("Туман", "🌫️")
        51 -> WeatherCondition("Слабая морось", "🌦️")
        53, 55 -> WeatherCondition("Морось", "🌧️")
        56, 57 -> WeatherCondition("Ледяная морось", "🌧️")
        61 -> WeatherCondition("Небольшой дождь", "🌦️")
        63 -> WeatherCondition("Дождь", "🌧️")
        65 -> WeatherCondition("Сильный дождь", "🌧️")
        66, 67 -> WeatherCondition("Ледяной дождь", "🌧️")
        71 -> WeatherCondition("Небольшой снег", "🌨️")
        73, 75 -> WeatherCondition("Снег", "❄️")
        77 -> WeatherCondition("Снежные зёрна", "❄️")
        80, 81, 82 -> WeatherCondition("Ливень", "🌧️")
        85, 86 -> WeatherCondition("Снежный ливень", "🌨️")
        95, 97 -> WeatherCondition("Гроза", "⛈️")
        96, 99 -> WeatherCondition("Гроза с градом", "⛈️")
        else -> WeatherCondition("Неизвестно", "❔")
    }

    fun map(response: WeatherResponse, city: City): WeatherUiModel {
        val current = requireNotNull(response.current) { "Нет текущей погоды" }
        val temperature = number(current.temperature)
        val wind = number(current.windSpeed)
        require(wind >= 0) { "Отрицательная скорость ветра" }
        val daily = requireNotNull(response.daily) { "Нет дневного прогноза" }
        val dates = requireNotNull(daily.time)
        val minimum = requireNotNull(daily.minimum)
        val maximum = requireNotNull(daily.maximum)
        require(dates.isNotEmpty() && dates.size == minimum.size && dates.size == maximum.size)
        val codes = daily.codes
        require(codes == null || codes.size == dates.size)
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
        val output = SimpleDateFormat("EEE, d MMM", Locale.forLanguageTag("ru-RU"))
        val days = dates.indices.map { index ->
            val date = requireNotNull(dates[index])
            require(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(date))
            val low = number(minimum[index])
            val high = number(maximum[index])
            require(low <= high)
            ForecastDay(output.format(requireNotNull(input.parse(date))), low, high,
                condition(codes?.get(index)))
        }
        val updated = current.time?.replace('T', ' ') ?: "Время не указано"
        return WeatherUiModel(city.name, temperature, wind, condition(current.code),
            "$updated ${response.timezone.orEmpty()}".trim(), days)
    }

    private fun number(value: Double?): Double = requireNotNull(value).also {
        require(it.isFinite()) { "Некорректное число" }
    }
}
