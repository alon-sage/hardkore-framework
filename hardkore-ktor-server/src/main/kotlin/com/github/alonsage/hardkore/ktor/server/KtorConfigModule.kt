package com.github.alonsage.hardkore.ktor.server

import io.ktor.server.application.Application

fun interface KtorConfigModule : KtorModule {
    override fun invoke(appplication: Application) =
        appplication.configure()
}