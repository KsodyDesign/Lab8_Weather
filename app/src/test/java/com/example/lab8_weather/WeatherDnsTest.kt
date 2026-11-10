package com.example.lab8_weather

import okhttp3.Dns
import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress
import java.net.UnknownHostException

class WeatherDnsTest {
    @Test fun prefersIpv4WithoutDroppingIpv6() {
        val first = InetAddress.getByName("192.0.2.1")
        val second = InetAddress.getByName("192.0.2.2")
        val ipv6 = InetAddress.getByName("2001:db8::1")
        val dns = WeatherDns(object : Dns {
            override fun lookup(hostname: String) = listOf(ipv6, first, second)
        })
        assertEquals(listOf(first, second, ipv6), dns.lookup("api.open-meteo.com"))
    }

    @Test(expected = UnknownHostException::class)
    fun propagatesResolverErrors() {
        WeatherDns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> = throw UnknownHostException(hostname)
        }).lookup("api.open-meteo.com")
    }
}
