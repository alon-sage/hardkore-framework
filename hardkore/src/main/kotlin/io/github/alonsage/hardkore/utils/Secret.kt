package io.github.alonsage.hardkore.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@JvmInline
value class Secret(val value: String) {
    @OptIn(ExperimentalEncodingApi::class)
    fun base64decoded(decoder: Base64 = Base64.Default): ByteArray =
        decoder.decode(value)

    override fun toString(): String = "********"

    companion object {
        @OptIn(ExperimentalEncodingApi::class)
        fun base64encoded(value: ByteArray, encoder: Base64 = Base64.Default): Secret =
            Secret(encoder.encode(value))
    }
}