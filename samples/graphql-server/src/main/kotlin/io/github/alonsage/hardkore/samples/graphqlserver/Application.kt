package io.github.alonsage.hardkore.samples.graphqlserver

import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DiModule
import io.github.alonsage.hardkore.di.DiProfiles
import io.github.alonsage.hardkore.graphql.server.GraphQLServerDiProfile
import io.github.alonsage.hardkore.graphql.server.dataclasses.Query
import io.github.alonsage.hardkore.graphql.server.dataclasses.Subscription
import io.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLDataClass
import io.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLResolver
import io.github.alonsage.hardkore.runtime.runApplication
import com.google.auto.service.AutoService
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
        bindGraphQLDataClass<Event>()
        bindGraphQLResolver { DummyResolver() }
    }
}

data class User(val id: UUID, val name: String)

data class Event(val id: UUID, val message: String)

// A dummy resolver that mocks DB access
class DummyResolver {
    // Loads user from DB
    suspend fun Query.user(id: UUID): User =
        User(id, "foo")

    // Load number of events from DB
    suspend fun User.lastEvents(number: Int): List<Event> =
        List(number) { Event(UUID.randomUUID(), "event-$it") }

    // Watches and load new events from DB
    fun Subscription.userEvents(userId: UUID): Flow<Event> =
        flow {
            var index = 0
            while (true) {
                emit(Event(UUID.randomUUID(), "event-$userId-$index"))
                index += 1
                delay(100)
            }
        }
}

fun main(args: Array<String>) {
    // Discovers CLI commands and run appropriate
    runApplication(args)
}