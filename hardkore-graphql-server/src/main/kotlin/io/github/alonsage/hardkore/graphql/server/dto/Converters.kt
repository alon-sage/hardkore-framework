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

internal fun JsonElement.toKotlin(path: String, overrides: Map<String, Any?>): Any? =
    when (this) {
        is JsonPrimitive -> toKotlin(path, overrides)
        is JsonArray -> if (path in overrides) overrides[path] else toKotlin(path, overrides)
        is JsonObject -> if (path in overrides) overrides[path] else toKotlin(path, overrides)
    }

internal fun JsonPrimitive.toKotlin(path: String, overrides: Map<String, Any?>): Any? =
    if (path in overrides) overrides[path] else {
        if (this == JsonNull) null
        else if (isString) content
        else booleanOrNull ?: intOrNull ?: doubleOrNull ?: contentOrNull
    }

internal fun JsonArray.toKotlin(path: String, overrides: Map<String, Any?>): List<Any?> =
    mapIndexed { index, item -> item.toKotlin("${path}.${index}", overrides) }

internal fun JsonObject.toKotlin(path: String, overrides: Map<String, Any?>): Map<String, Any?> =
    mapValues { (key, value) -> value.toKotlin("${path}.${key}", overrides) }

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