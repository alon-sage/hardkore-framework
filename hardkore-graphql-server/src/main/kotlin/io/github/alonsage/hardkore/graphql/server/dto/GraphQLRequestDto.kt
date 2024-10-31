package io.github.alonsage.hardkore.graphql.server.dto

import graphql.ExecutionInput
import io.ktor.http.content.PartData
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.dataloader.DataLoaderRegistry

@Serializable
data class GraphQLRequestDto(
    val query: String,
    val operationName: String?,
    val variables: JsonObject?,
    val extensions: JsonObject?
) {
    fun executionInput(
        dataLoaderRegistry: DataLoaderRegistry,
        coroutineScope: CoroutineScope,
        context: Map<Any?, Any?> = emptyMap(),
        fileOverrides: Map<String, PartData.FileItem> = emptyMap(),
    ): ExecutionInput =
        ExecutionInput.newExecutionInput()
            .query(query)
            .operationName(operationName)
            .variables(variables?.toKotlin("variables", fileOverrides).orEmpty())
            .extensions(extensions?.toKotlin("extensions", fileOverrides).orEmpty())
            .dataLoaderRegistry(dataLoaderRegistry)
            .graphQLContext(context + mapOf(CoroutineScope::class to coroutineScope))
            .build()
}