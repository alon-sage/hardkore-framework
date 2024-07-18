package com.github.alonsage.hardkore.fqdn.codec

interface FqdnNodeCodec<T> {
    fun encode(value: T): String
    fun decode(value: String): T
}
