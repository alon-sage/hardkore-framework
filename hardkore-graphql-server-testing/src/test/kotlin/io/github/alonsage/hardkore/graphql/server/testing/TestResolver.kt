package io.github.alonsage.hardkore.graphql.server.testing

import io.github.alonsage.hardkore.graphql.server.dataclasses.Mutation
import io.github.alonsage.hardkore.graphql.server.dataclasses.Query
import io.github.alonsage.hardkore.graphql.server.dataclasses.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestResolver {
    fun Query.echo(value: String): String =
        value

    fun Mutation.echo(value: String): String =
        value

    fun Subscription.echo(value: String): Flow<String> =
        flow { repeat(10) { emit(value) } }
}