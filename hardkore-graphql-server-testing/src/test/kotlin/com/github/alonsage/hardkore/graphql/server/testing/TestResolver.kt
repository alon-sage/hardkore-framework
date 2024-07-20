package com.github.alonsage.hardkore.graphql.server.testing

import com.github.alonsage.hardkore.graphql.server.dataclasses.Mutation
import com.github.alonsage.hardkore.graphql.server.dataclasses.Query
import com.github.alonsage.hardkore.graphql.server.dataclasses.Subscription
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