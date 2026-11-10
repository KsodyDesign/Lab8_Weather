package com.example.lab8_weather

import org.junit.Assert.*
import org.junit.Test

class WeatherMapperTest {
    private fun response() = WeatherResponse(CurrentWeather(-3.2, 12.4, 3, "2026-11-10T14:00"),
        DailyWeather(listOf("2026-11-10", "2026-11-11"), listOf(2.0, 3.0), listOf(-4.0, -2.0), listOf(71, 0)), "GMT+7")

    @Test fun mapsCurrentWeatherAndDailyForecast() {
        val model = WeatherMapper.map(response(), City.all.first())
        assertEquals(-3.2, model.temperature, 0.0)
        assertEquals(12.4, model.windSpeed, 0.0)
        assertEquals("Облачно", model.condition.description)
        assertEquals(2, model.days.size)
        assertEquals(-4.0, model.days[0].minimum, 0.0)
        assertEquals(2.0, model.days[0].maximum, 0.0)
        assertEquals("Небольшой снег", model.days[0].condition.description)
    }
    @Test fun handlesUnknownAndMissingWeatherCodes() {
        assertEquals("Неизвестно", WeatherMapper.condition(999).description)
        assertEquals("Неизвестно", WeatherMapper.condition(null).description)
    }
    @Test fun distinguishesDrizzleFromRainAndHail() {
        assertEquals("Слабая морось", WeatherMapper.condition(51).description)
        assertEquals("Дождь", WeatherMapper.condition(63).description)
        assertEquals("Гроза с градом", WeatherMapper.condition(96).description)
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsMissingCurrentWeather() { WeatherMapper.map(response().copy(current = null), City.all.first()) }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonFiniteTemperature() {
        WeatherMapper.map(response().let { it.copy(current = it.current!!.copy(temperature = Double.NaN)) }, City.all.first())
    }
    @Test(expected = IllegalArgumentException::class)
    fun rejectsTruncatedForecast() {
        WeatherMapper.map(response().let { it.copy(daily = it.daily!!.copy(minimum = emptyList())) }, City.all.first())
    }
    @Test(expected = java.text.ParseException::class)
    fun rejectsInvalidDate() {
        WeatherMapper.map(response().let { it.copy(daily = it.daily!!.copy(time = listOf("2026-02-30", "2026-11-11"))) }, City.all.first())
    }
}
