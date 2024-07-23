package io.github.alonsage.hardkore.graphql.server.dto

import graphql.ExecutionInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import org.dataloader.DataLoaderRegistry

@Serializable
data class GraphQLRequestDto(
    val query: String,
    val operationName: String?,
    val variables: Map<String, JsonElement>?,
    val extensions: Map<String, JsonElement>?
) {
    fun executionInput(
        dataLoaderRegistry: DataLoaderRegistry,
        coroutineScope: CoroutineScope,
        context: Map<Any?, Any?> = emptyMap()
    ): ExecutionInput =
        ExecutionInput.newExecutionInput()
            .query(query)
            .operationName(operationName)
            .variables(variables?.mapValues { (_, value) -> value.toKotlin() }.orEmpty())
            .extensions(extensions?.mapValues { (_, value) -> value.toKotlin() }.orEmpty())
            .dataLoaderRegistry(dataLoaderRegistry)
            .graphQLContext(context + mapOf(CoroutineScope::class to coroutineScope))
            .build()
}