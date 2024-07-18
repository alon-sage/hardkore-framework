package com.github.alonsage.hardkore.graphql.server.testing

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.network.http.KtorHttpEngine
import com.apollographql.apollo3.network.ws.KtorWebSocketEngine
import io.ktor.client.HttpClient

fun HttpClient.graphql(): ApolloClient =
    ApolloClient.Builder()
        .serverUrl("/graphql")
        .httpEngine(@OptIn(ApolloExperimental::class) KtorHttpEngine(this))
        .webSocketEngine(@OptIn(ApolloExperimental::class) KtorWebSocketEngine(this))
        .build()