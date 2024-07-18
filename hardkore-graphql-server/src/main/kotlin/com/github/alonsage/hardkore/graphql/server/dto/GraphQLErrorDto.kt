package com.github.alonsage.hardkore.graphql.server.dto

import graphql.GraphQLError
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GraphQLErrorDto(
    val message: String,
    val locations: List<GraphQLSourceLocationDto>?,
    val path: List<JsonElement>?,
    val extensions: Map<String, JsonElement>?
) {
    constructor(error: GraphQLError) : this(
        message = error.message,
        locations = error.locations?.map(::GraphQLSourceLocationDto),
        path = error.path?.map { it.toJsonElement() },
        extensions = error.extensions?.mapValues { (_, value) -> value.toJsonElement() }
    )
}