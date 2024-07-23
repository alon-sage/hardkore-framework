package io.github.alonsage.hardkore.ktor.server.starter

import com.typesafe.config.ConfigFactory
import kotlin.random.Random

inline fun withConfiguredHostPort(block: (Pair<String, Int>) -> Unit) {
    val host = "127.0.0.1"
    val port = Random.nextInt(32768, 65535)
    System.setProperty("ktor.listen.0.host", host)
    System.setProperty("ktor.listen.0.port", port.toString())
    ConfigFactory.invalidateCaches()
    try {
        block(host to port)
    } finally {
        System.clearProperty("ktor.listen.0.host")
        System.clearProperty("ktor.listen.0.port")
    }
}