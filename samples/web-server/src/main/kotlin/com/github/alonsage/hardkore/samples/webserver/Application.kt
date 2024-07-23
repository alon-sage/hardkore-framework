package com.github.alonsage.hardkore.samples.webserver

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.DiProfiles
import com.github.alonsage.hardkore.ktor.server.KtorModule
import com.github.alonsage.hardkore.ktor.server.KtorServerDiProfile
import com.github.alonsage.hardkore.ktor.server.bindKtorModule
import com.github.alonsage.hardkore.runtime.runApplication
import com.google.auto.service.AutoService
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

// Makes the module visible for auto discovering
@AutoService(DiModule::class)
// Makes the module discoverable only for the Ktor server profile
@DiProfiles(KtorServerDiProfile::class)
class SampleDiModule : DiModule {
    override fun Binder.install() {
        // Binds sample Ktor module to DI context
        bindKtorModule { sampleKtorModule() }
    }

    private fun sampleKtorModule() = KtorModule {
        // Regular Ktor DSL here
        routing {
            get("/greeting") {
                val name = call.request.queryParameters["name"] ?: "World"
                call.respondText("Hello $name!")
            }
        }
    }
}

fun main(args: Array<String>) {
    // Discovers CLI commands and run appropriate
    runApplication(args)
}