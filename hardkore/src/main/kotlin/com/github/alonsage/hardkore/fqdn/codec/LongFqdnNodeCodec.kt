package com.github.alonsage.hardkore.fqdn.codec

import com.google.auto.service.AutoService

@AutoService(FqdnNodeCodec::class)
@SuppressWarnings("rawtypes")
class LongFqdnNodeCodec : FqdnNodeCodec<Long> {
    override fun encode(value: Long): String = value.toString()
    override fun decode(value: String): Long = value.toLong()
}