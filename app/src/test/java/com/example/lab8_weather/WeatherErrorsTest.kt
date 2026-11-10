package com.example.lab8_weather

import org.junit.Assert.*
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class WeatherErrorsTest {
    @Test fun certificateFailureIsNotReportedAsMissingInternet() {
        assertTrue(WeatherErrors.message(SSLHandshakeException("Trust anchor not found")).contains("дату и время"))
    }
    @Test fun explainsDnsFailure() {
        assertTrue(WeatherErrors.message(UnknownHostException()).contains("найти сервер"))
    }
    @Test fun explainsTimeout() {
        assertTrue(WeatherErrors.message(SocketTimeoutException()).contains("не ответил вовремя"))
    }
}
