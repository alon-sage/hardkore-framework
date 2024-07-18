package com.github.alonsage.hardkore.logging

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.google.auto.service.AutoService
import org.slf4j.bridge.SLF4JBridgeHandler

@AutoService(DiModule::class)
class LoggingDiModule : DiModule {
    init {
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()
    }

    override fun Binder.install() {}
}