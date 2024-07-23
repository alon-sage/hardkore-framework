package io.github.alonsage.hardkore.graphql.server.dto

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

internal fun JsonElement.toKotlin(): Any? =
    when (this) {
        JsonNull -> null
        is JsonPrimitive -> if (isString) content else booleanOrNull ?: intOrNull ?: doubleOrNull ?: contentOrNull
        is JsonArray -> map { item -> item.toKotlin() }
        is JsonObject -> mapValues { (_, value) -> value.toKotlin() }
    }

@Suppress("UNCHECKED_CAST")
internal fun Any?.toJsonElement(): JsonElement =
    when (this) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        is List<*> -> JsonArray(map { it.toJsonElement() })
        is Map<*, *> -> JsonObject(mapValues { (_, value) -> value.toJsonElement() } as Map<String, JsonElement>)
        else -> error("Unsupported data type: ${this::class}")
    }