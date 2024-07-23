package io.github.alonsage.hardkore.fqdn.codec

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertNotNull

class FqdnNodeCodecsTest {
    @Test
    fun `throws on unsupported node type`() {
        val exception = assertThrows<IllegalStateException> {
            FqdnNodeCodecs.codec(FqdnNodeCodecsTest::class)
        }
        assertContains(assertNotNull(exception.message), "Missing codec for type:")
    }
}