package com.example.lab8_weather

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.ViewModelProvider
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherScreenTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private lateinit var activity: MainActivity
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun launch() {
        activity = instrumentation.startActivitySync(Intent(instrumentation.targetContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) as MainActivity
        instrumentation.waitForIdleSync()
    }
    private fun current() {
        main { activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).first() as MainActivity }
    }
    private fun waitFor(timeout: Long = 35_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeout
        while (System.currentTimeMillis() < deadline) {
            var success = false
            main { success = condition() }
            if (success) return
            Thread.sleep(200)
        }
        fail("Timed out waiting for app state")
    }
    private fun screenshot(name: String) {
        instrumentation.waitForIdleSync()
        Thread.sleep(800)
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "checks/$name.png")
        file.parentFile!!.mkdirs()
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    private fun rotate() {
        val old = activity
        main { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        Thread.sleep(2000)
        current()
        assertNotSame(old, activity)
    }
    private fun shell(command: String) {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    }

    private fun model() = ViewModelProvider(activity)[WeatherViewModel::class.java]
    @Test fun loadsForecastUsingTls12AndOnlyLegacyRootCertificate() {
        val context = instrumentation.targetContext
        val certificate = context.resources.openRawResource(R.raw.isrg_root_x1).use {
            java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(it)
        }
        val store = java.security.KeyStore.getInstance(java.security.KeyStore.getDefaultType()).apply {
            load(null)
            setCertificateEntry("isrg-root-x1", certificate)
        }
        val factory = javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm()).apply { init(store) }
        val trust = factory.trustManagers.single() as javax.net.ssl.X509TrustManager
        val tls = javax.net.ssl.SSLContext.getInstance("TLS").apply { init(null, arrayOf(trust), null) }
        val client = okhttp3.OkHttpClient.Builder()
            .sslSocketFactory(tls.socketFactory, trust)
            .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
            .dns(WeatherDns())
            .connectionSpecs(listOf(okhttp3.ConnectionSpec.Builder(okhttp3.ConnectionSpec.MODERN_TLS)
                .tlsVersions(okhttp3.TlsVersion.TLS_1_2).build()))
            .callTimeout(25, java.util.concurrent.TimeUnit.SECONDS).build()
        try {
            val api = retrofit2.Retrofit.Builder().baseUrl("https://api.open-meteo.com/")
                .client(client).addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build().create(WeatherApiService::class.java)
            val response = kotlinx.coroutines.runBlocking { api.forecast(55.75, 37.62) }
            assertEquals(5, WeatherMapper.map(response, City.all[1]).days.size)
        } finally {
            client.connectionPool.evictAll()
            client.dispatcher.executorService.shutdown()
        }
    }

    @Test fun loadsCitiesSurvivesRotationAndHandlesOffline() {
        shell("svc wifi enable")
        shell("svc data enable")
        launch()
        waitFor { model().state.value !is WeatherUiState.Loading }
        var state = model().state.value
        assertTrue("Real API request must succeed: $state", state is WeatherUiState.Success)
        val initial = (state as WeatherUiState.Success).model
        assertEquals("Новосибирск", initial.city)
        assertEquals(5, initial.days.size)
        assertTrue(activity.findViewById<android.widget.TextView>(R.id.temperatureText).text.contains("°C"))
        screenshot("novosibirsk")
        // Compare with another request to the same service and coordinates.
        val raw = kotlinx.coroutines.runBlocking { WeatherApi.service.forecast(55.03, 82.92) }
        assertEquals(raw.current!!.temperature!!, initial.temperature, 0.2)
        assertEquals(raw.current!!.windSpeed!!, initial.windSpeed, 0.3)
        main { activity.findViewById<android.widget.Spinner>(R.id.citySpinner).setSelection(1) }
        waitFor { (model().state.value as? WeatherUiState.Success)?.model?.city == "Москва" }
        screenshot("moscow")
        val before = model()
        val beforeState = before.state.value
        rotate()
        assertSame(before, model())
        assertEquals(beforeState, model().state.value)
        screenshot("rotated")
        try {
            shell("svc wifi disable")
            shell("svc data disable")
            Thread.sleep(1200)
            main { activity.findViewById<android.widget.Button>(R.id.refreshButton).performClick() }
            waitFor { model().state.value is WeatherUiState.Error }
            assertEquals(android.view.View.VISIBLE, activity.findViewById<android.view.View>(R.id.errorText).visibility)
            screenshot("offline")
        } finally {
            shell("svc wifi enable")
            shell("svc data enable")
        }
        waitFor(20_000) {
            val connectivity = activity.getSystemService(android.net.ConnectivityManager::class.java)
            connectivity.getNetworkCapabilities(connectivity.activeNetwork)
                ?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        }
        main { activity.findViewById<android.widget.Button>(R.id.refreshButton).performClick() }
        waitFor { model().state.value is WeatherUiState.Success }
        main { activity.finish() }
    }
}
