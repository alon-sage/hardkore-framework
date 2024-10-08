package io.github.alonsage.hardkore.fqdn.codec

import com.google.auto.service.AutoService
import java.util.*

@AutoService(FqdnNodeCodec::class)
@SuppressWarnings("rawtypes")
class UuidFqdnNodeCodec : FqdnNodeCodec<UUID> {
    override fun encode(value: UUID): String = value.toString()
    override fun decode(value: String): UUID = UUID.fromString(value)
}