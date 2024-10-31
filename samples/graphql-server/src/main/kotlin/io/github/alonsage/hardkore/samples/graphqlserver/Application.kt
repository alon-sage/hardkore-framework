package io.github.alonsage.hardkore.samples.graphqlserver

import com.google.auto.service.AutoService
import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DiModule
import io.github.alonsage.hardkore.di.DiProfiles
import io.github.alonsage.hardkore.graphql.server.GraphQLServerDiProfile
import io.github.alonsage.hardkore.graphql.server.dataclasses.Mutation
import io.github.alonsage.hardkore.graphql.server.dataclasses.Query
import io.github.alonsage.hardkore.graphql.server.dataclasses.Subscription
import io.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLDataClass
import io.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLEnum
import io.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLResolver
import io.github.alonsage.hardkore.runtime.runApplication
import io.ktor.http.content.PartData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

// Makes the module visible for auto discovering
@AutoService(DiModule::class)
// Makes the module discoverable only for the Ktor server profile
@DiProfiles(GraphQLServerDiProfile::class)
class SampleDiModule : DiModule {
    override fun Binder.install() {
        bindGraphQLDataClass<User>()
        bindGraphQLDataClass<FileInfo>()
        bindGraphQLEnum<EventSource>()
        bindGraphQLDataClass<Event>()
        bindGraphQLResolver { DummyResolver() }
    }
}

data class User(val id: UUID, val name: String)

enum class EventSource { SYSTEM, USER }

data class Event(val id: UUID, val message: String, val source: EventSource)

data class FileInfo(val name: String, val type: String, val content: String)

// A dummy resolver that mocks DB access
class DummyResolver {
    // Loads user from DB
    suspend fun Query.user(id: UUID): User =
        User(id, "foo")

    // Load number of events from DB
    suspend fun User.lastEvents(number: Int): List<Event> =
        List(number) { Event(UUID.randomUUID(), "event-$it", EventSource.entries.random()) }

    // Emit a new event
    @Suppress("UNUSED_PARAMETER")
    suspend fun Mutation.emitEvent(userId: UUID, message: String, source: EventSource): Event =
        Event(UUID.randomUUID(), message, source)

    // Upload file to storage
    suspend fun Mutation.upload(file: PartData.FileItem): FileInfo =
        FileInfo(
            name = file.originalFileName ?: "",
            type = file.contentType.toString(),
            content = file.provider().readText()
        )

    // Watches and load new events from DB
    fun Subscription.userEvents(userId: UUID): Flow<Event> =
        flow {
            var index = 0
            while (true) {
                emit(Event(UUID.randomUUID(), "event-$userId-$index", EventSource.entries.random()))
                index += 1
                delay(100)
            }
        }
}

fun main(args: Array<String>) {
    // Discovers CLI commands and run appropriate
    runApplication(args)
}