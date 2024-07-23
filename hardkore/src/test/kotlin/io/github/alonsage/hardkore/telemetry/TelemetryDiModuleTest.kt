package io.github.alonsage.hardkore.telemetry

import io.github.alonsage.hardkore.di.DiContext
import io.github.alonsage.hardkore.di.bean
import io.opentelemetry.api.OpenTelemetry
import org.junit.jupiter.api.Test

class TelemetryDiModuleTest {
    @Test
    fun `OpenTelemetry instance available`() {
        val context = DiContext { install(TelemetryDiModule()) }
        context.bean<OpenTelemetry>()
    }
}