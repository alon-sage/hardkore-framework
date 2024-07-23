package io.github.alonsage.hardkore.logging

import io.github.alonsage.hardkore.di.DiContext
import org.junit.jupiter.api.Test
import org.slf4j.bridge.SLF4JBridgeHandler
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoggingDiModuleTest {
    @Test
    fun `SLF4JBridge installed`() {
        SLF4JBridgeHandler.uninstall()
        assertFalse(SLF4JBridgeHandler.isInstalled())

        DiContext { install(LoggingDiModule()) }
        assertTrue(SLF4JBridgeHandler.isInstalled())
    }
}