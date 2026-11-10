package com.example.lab8_weather

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val api: WeatherApiService = WeatherApi.service) {
    suspend fun forecast(city: City): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            Result.success(api.forecast(city.latitude, city.longitude))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
