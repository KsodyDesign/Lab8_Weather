package com.example.lab8_weather

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: WeatherRepository
    @Before fun setUp() {
        server = MockWebServer().apply { start() }
        val api = Retrofit.Builder().baseUrl(server.url("/"))
            .client(OkHttpClient.Builder().readTimeout(300, TimeUnit.MILLISECONDS).build())
            .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherApiService::class.java)
        repository = WeatherRepository(api)
    }
    @After fun tearDown() { server.shutdown() }
    @Test fun parsesResponseAndSendsCityCoordinates() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"current_weather":{"temperature":4.2,"windspeed":10.5,"weathercode":0},"daily":{"time":["2026-11-10"],"temperature_2m_max":[5.0],"temperature_2m_min":[1.0]}}"""))
        val result = repository.forecast(City.all[1])
        assertTrue(result.isSuccess)
        assertEquals(4.2, result.getOrThrow().current!!.temperature!!, 0.0)
        val url = server.takeRequest().requestUrl!!
        assertEquals("55.75", url.queryParameter("latitude"))
        assertEquals("37.62", url.queryParameter("longitude"))
        assertEquals("true", url.queryParameter("current_weather"))
        assertEquals("5", url.queryParameter("forecast_days"))
    }
    @Test fun returnsHttpError() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503))
        assertTrue(repository.forecast(City.all[0]).exceptionOrNull() is HttpException)
    }
    @Test fun returnsMalformedJsonError() = runBlocking {
        server.enqueue(MockResponse().setBody("not json"))
        assertTrue(repository.forecast(City.all[0]).isFailure)
    }
    @Test fun returnsNetworkTimeout() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        assertTrue(repository.forecast(City.all[0]).exceptionOrNull() is IOException)
    }
    @Test fun propagatesCancellation() = runBlocking {
        server.enqueue(MockResponse().setBody("{}").setBodyDelay(5, TimeUnit.SECONDS))
        var cancelled = false
        try { withTimeout(100) { repository.forecast(City.all[0]) } }
        catch (error: CancellationException) { cancelled = true }
        assertTrue(cancelled)
    }
}
