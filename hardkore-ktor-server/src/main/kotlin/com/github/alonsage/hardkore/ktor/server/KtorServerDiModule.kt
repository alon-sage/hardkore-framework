package com.github.alonsage.hardkore.ktor.server

import com.github.alonsage.hardkore.condition.Conditions
import com.github.alonsage.hardkore.config.bindConfigBean
import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.DiProfiles
import com.github.alonsage.hardkore.di.bean
import com.github.alonsage.hardkore.di.bindFactory
import com.github.alonsage.hardkore.di.bindSet
import com.github.alonsage.hardkore.runtime.RuntimeService
import com.github.alonsage.hardkore.runtime.bindRuntimeService
import com.google.auto.service.AutoService
import com.typesafe.config.Config
import io.ktor.server.application.ServerReady
import io.ktor.server.application.call
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

@AutoService(DiModule::class)
@DiProfiles(KtorServerDiProfile::class)
class KtorServerDiModule : DiModule {
    override fun Binder.install() {
        bindConfigBean<Properties>("ktor")
        bindSet<KtorModule> {}
        bindFactory { applicationEngineEnvironment(bean(), bean(), bean(), bean()) }
        bindRuntimeService { serverRuntimeService(bean()) }
    }

    private fun applicationEngineEnvironment(
        properties: Properties,
        appConfig: Config,
        appModules: Set<KtorModule>,
        conditions: Conditions
    ): ApplicationEngineEnvironment =
        applicationEngineEnvironment {
            log = LoggerFactory.getLogger(KtorServerDiModule::class.java)
            config = HoconApplicationConfig(appConfig.withOnlyPath("ktor"))

            rootPath = properties.rootPath
            properties.listen.forEach {
                connector {
                    host = it.host
                    port = it.port
                }
            }

            module {
                environment.monitor.subscribe(ServerReady) {
                    conditions.set(KtorServerReady)
                }
            }

            val (configModules, otherModules) = appModules.partition { it is KtorConfigModule }
            configModules.forEach { module(it) }
            otherModules.forEach { module(it) }
            if (properties.healthCheck) {
                module { routing { get("/health") { call.respondText("OK") } } }
            }
        }

    private fun serverRuntimeService(environment: ApplicationEngineEnvironment): RuntimeService =
        RuntimeService {
            supervisorScope {
                val runtimeEnvironment = object : ApplicationEngineEnvironment by environment {
                    override val parentCoroutineContext: CoroutineContext = coroutineContext
                }
                val engine = Netty.create(runtimeEnvironment) {}
                engine.start()
            }
        }

    data class Properties(
        val rootPath: String,
        val listen: List<Listener>,
        val healthCheck: Boolean
    ) {
        data class Listener(
            val host: String,
            val port: Int
        )
    }
}