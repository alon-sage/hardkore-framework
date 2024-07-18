package com.github.alonsage.hardkore.telemetry

import com.github.alonsage.hardkore.di.DiContext
import com.github.alonsage.hardkore.di.bean
import io.opentelemetry.api.OpenTelemetry
import org.junit.jupiter.api.Test

class TelemetryDiModuleTest {
    @Test
    fun `OpenTelemetry instance available`() {
        val context = DiContext { install(TelemetryDiModule()) }
        context.bean<OpenTelemetry>()
    }
}