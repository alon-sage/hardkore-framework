package com.github.alonsage.hardkore.ktor.server

import io.ktor.server.application.Application

fun interface KtorModule : (Application) -> Unit {
    fun Application.configure()

    override fun invoke(appplication: Application) =
        appplication.configure()
}
