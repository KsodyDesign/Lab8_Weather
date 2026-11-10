package com.example.lab8_weather

import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress

/** Keep the system resolver and every address, but try IPv4 before broken IPv6 routes. */
class WeatherDns(private val delegate: Dns = Dns.SYSTEM) : Dns {
    override fun lookup(hostname: String): List<InetAddress> =
        delegate.lookup(hostname).sortedBy { if (it is Inet4Address) 0 else 1 }
}
