package com.github.alonsage.hardkore.ktor.server.testing

import com.github.alonsage.hardkore.di.DiContext
import com.github.alonsage.hardkore.di.DiProfile
import com.github.alonsage.hardkore.di.bean
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.server.testing.TestEngine
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

fun webApplicationTest(
    vararg requiredProfiles: KClass<out DiProfile>,
    block: suspend DiContext.(client: HttpClient) -> Unit
) {
    val context = DiContext { autoInstall(*requiredProfiles) }
    val engine = TestEngine.create(context.bean()) { }
    val client = engine.client.config {
        defaultRequest {
            url("http://test/")
        }
    }
    engine.start()
    try {
        runBlocking { context.block(client) }
    } finally {
        engine.stop()
    }
}