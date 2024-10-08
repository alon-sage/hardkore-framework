package io.github.alonsage.hardkore.graphql.server.dto

import graphql.ExecutionResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphQLResponseDto(
    val data: JsonElement,
    val errors: List<GraphQLErrorDto>?,
    val extensions: Map<String, JsonElement>?
) {
    constructor(result: ExecutionResult) : this(
        data = result.getData<JsonElement?>().toJsonElement(),
        errors = result.errors?.map(::GraphQLErrorDto)?.ifEmpty { null },
        extensions = result.extensions?.entries?.associate { (key, value) -> key as String to value.toJsonElement() }
    )
}