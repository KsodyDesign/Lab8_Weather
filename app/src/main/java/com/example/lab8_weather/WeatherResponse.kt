package com.example.lab8_weather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current_weather") val current: CurrentWeather? = null,
    val daily: DailyWeather? = null,
    @SerializedName("timezone_abbreviation") val timezone: String? = null
)

data class CurrentWeather(
    val temperature: Double? = null,
    @SerializedName("windspeed") val windSpeed: Double? = null,
    @SerializedName("weathercode") val code: Int? = null,
    val time: String? = null
)

data class DailyWeather(
    val time: List<String?>? = null,
    @SerializedName("temperature_2m_max") val maximum: List<Double?>? = null,
    @SerializedName("temperature_2m_min") val minimum: List<Double?>? = null,
    @SerializedName("weather_code") val codes: List<Int?>? = null
)

data class City(val name: String, val latitude: Double, val longitude: Double) {
    companion object {
        val all = listOf(
            City("Новосибирск", 55.03, 82.92),
            City("Москва", 55.75, 37.62),
            City("Санкт-Петербург", 59.94, 30.31)
        )
    }
}
