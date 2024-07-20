package com.github.alonsage.hardkore.ktor.server

import com.github.alonsage.hardkore.condition.Condition
import com.github.alonsage.hardkore.condition.Conditions
import com.github.alonsage.hardkore.di.DiContext
import com.github.alonsage.hardkore.di.bean
import com.github.alonsage.hardkore.runtime.RuntimeService
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.server.engine.ApplicationEngineEnvironment
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorServerDiModuleTest {
    @Test
    fun `components installed`() {
        val context = DiContext {
            autoInstall()
            install(KtorServerDiModule())
        }

        context.bean<ApplicationEngineEnvironment>()
        val services: Set<RuntimeService> = context.bean()
        assertTrue(services.isNotEmpty())
    }

    @Test
    fun `auto installation works`() {
        val context = DiContext {
            autoInstall(KtorServerDiProfile::class)
        }

        context.bean<ApplicationEngineEnvironment>()
        val services: Set<RuntimeService> = context.bean()
        assertTrue(services.isNotEmpty())
    }

    @Test
    fun `ktor modules installed`() {
        val configModuleInstalled = object : Condition {}
        val moduleInstalled = object : Condition {}

        val context = DiContext {
            autoInstall()
            install(KtorServerDiModule())
            bindKtorModule {
                val conditions: Conditions = bean()
                KtorConfigModule { conditions.set(configModuleInstalled) }
            }
            bindKtorModule {
                val conditions: Conditions = bean()
                KtorModule { conditions.set(moduleInstalled) }
            }
        }

        context.bean<ApplicationEngineEnvironment>().start()

        val conditions: Conditions = context.bean()
        assertTrue(conditions.all(configModuleInstalled, moduleInstalled))
    }

    @Test
    fun `ktor server responsible`() {
        withConfiguredHostPort { (host, port) ->
            val context = DiContext {
                autoInstall()
                install(KtorServerDiModule())
            }

            val environment = context.bean<ApplicationEngineEnvironment>()
            assertEquals(1, environment.connectors.size)
            environment.connectors.single().let {
                assertEquals(host, it.host)
                assertEquals(port, it.port)
            }

            val services: Set<RuntimeService> = context.bean()
            val conditions: Conditions = context.bean()
            runBlocking {
                val jobs = services.map { launch { it.run() } }

                conditions.wait(KtorServerReady)

                val client = HttpClient()
                val response = client.get("http://$host:$port/health")
                assertTrue(response.status.isSuccess())
                assertEquals("OK", response.bodyAsText())

                jobs.forEach { it.cancel() }
            }
        }
    }
}