package com.github.alonsage.hardkore.graphql.server.dto

import graphql.language.SourceLocation
import kotlinx.serialization.Serializable

@Serializable
data class GraphQLSourceLocationDto(
    val line: Int,
    val column: Int,
    val sourceName: String?
) {
    constructor(location: SourceLocation) : this(
        line = location.line,
        column = location.column,
        sourceName = location.sourceName
    )
}