package io.github.alonsage.hardkore.graphql.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLSubscriptionStatus(
    val code: Int,
    val reason: String
) {
    companion object {
        val SERVER_ERROR = GraphQLSubscriptionStatus(1011, "")
        val INVALID_MESSAGE = GraphQLSubscriptionStatus(4400, "Invalid operation")
        val UNAUTHORIZED = GraphQLSubscriptionStatus(4401, "Unauthorized")
        val FORBIDDEN = GraphQLSubscriptionStatus(4403, "Forbidden")
        val CONNECTION_INIT_TIMEOUT = GraphQLSubscriptionStatus(4408, "Connection initialisation timeout")
        val TOO_MANY_REQUESTS = GraphQLSubscriptionStatus(4429, "Too many initialisation requests")
        fun conflict(id: String) = GraphQLSubscriptionStatus(4409, "Subscriber for $id already exists")
    }
}