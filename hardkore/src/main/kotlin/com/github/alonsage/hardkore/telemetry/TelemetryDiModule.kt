package com.github.alonsage.hardkore.telemetry

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.bindInstance
import com.google.auto.service.AutoService
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.OpenTelemetry

@AutoService(DiModule::class)
class TelemetryDiModule : DiModule {
    override fun Binder.install() {
        bindInstance<OpenTelemetry>(GlobalOpenTelemetry.get())
    }
}