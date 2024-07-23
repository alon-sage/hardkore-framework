package io.github.alonsage.hardkore.graphql.server

data class GraphQLProperties(
    val schemas: List<String>,
    val federation: Boolean,
    val introspection: Boolean,
    val subscriptionTimeoutMillis: Long,
    val authentication: GraphQLAuthentication,
    val authProviders: List<String>
)