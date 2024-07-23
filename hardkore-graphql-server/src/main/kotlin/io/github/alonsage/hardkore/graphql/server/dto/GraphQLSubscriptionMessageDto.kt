package io.github.alonsage.hardkore.graphql.server.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

@Serializable
@JsonClassDiscriminator("type")
@OptIn(ExperimentalSerializationApi::class)
sealed interface GraphQLSubscriptionMessageDto {
    @Serializable
    @SerialName("connection_init")
    data class ConnectionInit(
        val payload: JsonElement = JsonNull
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("connection_ack")
    data class ConnectionAck(
        val payload: JsonElement = JsonNull
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("ping")
    data class Ping(
        val payload: JsonElement = JsonNull
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("pong")
    data class Pong(
        val payload: JsonElement = JsonNull
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("subscribe")
    data class Subscribe(
        val id: String,
        val payload: GraphQLRequestDto
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("next")
    data class Next(
        val id: String,
        val payload: GraphQLResponseDto
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("error")
    data class Error(
        val id: String,
        val payload: List<GraphQLErrorDto>
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("complete")
    data class Complete(
        val id: String
    ) : GraphQLSubscriptionMessageDto

    @Serializable
    @SerialName("invalid")
    data class Invalid(
        val id: String,
        val payload: JsonElement = JsonNull
    ) : GraphQLSubscriptionMessageDto
}