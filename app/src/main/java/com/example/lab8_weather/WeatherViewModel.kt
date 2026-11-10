package com.example.lab8_weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val model: WeatherUiModel) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(private val repository: WeatherRepository = WeatherRepository()) : ViewModel() {
    private val mutableState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val state = mutableState.asStateFlow()
    var selectedCity: Int = -1
        private set
    private var request: Job? = null

    init { loadCity(0) }

    fun loadCity(index: Int, force: Boolean = false) {
        if (index !in City.all.indices || (!force && index == selectedCity)) return
        selectedCity = index
        request?.cancel()
        mutableState.value = WeatherUiState.Loading
        request = viewModelScope.launch {
            repository.forecast(City.all[index]).fold(
                onSuccess = { response ->
                    mutableState.value = try {
                        val model = WeatherMapper.map(response, City.all[index])
                        if (BuildConfig.DEBUG) Log.d("Weather", "code=${response.current?.code}, condition=${model.condition.description}")
                        WeatherUiState.Success(model)
                    } catch (error: Exception) {
                        WeatherUiState.Error("Сервис вернул некорректные данные. Попробуйте обновить погоду.")
                    }
                },
                onFailure = { error ->
                    if (BuildConfig.DEBUG) Log.w("Weather", "Forecast request failed", error)
                    val message = when (error) {
                        is HttpException -> "Сервис погоды временно недоступен (${error.code()}). Повторите запрос позже."
                        is JsonParseException -> "Не удалось прочитать ответ сервиса погоды."
                        is IOException -> "Не удалось подключиться к сервису. Проверьте интернет и повторите запрос."
                        else -> "Не удалось загрузить погоду. Попробуйте ещё раз."
                    }
                    mutableState.value = WeatherUiState.Error(message)
                }
            )
        }
    }

    fun refresh() = loadCity(selectedCity.coerceAtLeast(0), force = true)
}
