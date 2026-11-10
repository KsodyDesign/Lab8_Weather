package com.example.lab8_weather

import com.google.gson.JsonParseException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object WeatherErrors {
    fun message(error: Throwable): String = when (error) {
        is HttpException -> "Сервис погоды временно недоступен (${error.code()}). Повторите запрос позже."
        is JsonParseException -> "Не удалось прочитать ответ сервиса погоды."
        is UnknownHostException -> "Не удалось найти сервер погоды. Проверьте Wi-Fi или мобильный интернет."
        is SSLException -> "Не удалось установить защищённое соединение. Проверьте дату и время на телефоне."
        is SocketTimeoutException -> "Сервер погоды не ответил вовремя. Попробуйте другую сеть или повторите запрос."
        is ConnectException -> "Нет соединения с сервером погоды. Попробуйте Wi-Fi или мобильный интернет."
        is IOException -> "Соединение с сервисом погоды прервано. Повторите запрос."
        else -> "Не удалось загрузить погоду. Попробуйте ещё раз."
    }
}
