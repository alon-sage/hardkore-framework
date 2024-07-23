# HardKore

HardKore is a set of Kotlin libraries that helps develop and maintain backend applications. It covers configuration,
inversion of control, lifecycle, testing, and observability, and implements integration with some popular libraries like
Ktor and GraphQL Java.

HardKore built on the next principles:

* Intensive utilization of Kotlin language features
* First-class Kotlin coroutines support
* Modularization and extensibility
* Incorporating widely recognized libraries

## Table of contents

<!-- TOC -->
* [HardKore](#hardkore)
  * [Table of contents](#table-of-contents)
  * [Quick Start](#quick-start)
    * [Web Server](#web-server)
    * [GraphQL Server](#graphql-server)
  * [Packages:](#packages)
<!-- TOC -->

## Quick Start

### Web Server

[Source code](samples/web-server)

```kotlin
// build.gradle.kts

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    maven {
        name = "HardKore"
        url = uri("https://maven.pkg.github.com/alon-sage/hardkore-framework")
    }
}

dependencies {
    // Enable processing @AutoService annotation
    kapt("com.google.auto.service:auto-service")

    implementation("com.github.alon-sage.hardkore:hardkore-ktor-server-starter:0.1.1")

    testImplementation("com.github.alon-sage.hardkore:hardkore-ktor-server-testing:0.1.1")
}
```

```kotlin
// Application.kt

@AutoService(DiModule::class)
@DiProfiles(KtorServerDiProfile::class)
class SampleDiModule : DiModule {
    override fun Binder.install() {
        bindKtorModule { sampleKtorModule() }
    }

    private fun sampleKtorModule() = KtorModule {
        routing {
            get("/greeting") {
                val name = call.request.queryParameters["name"] ?: "World"
                call.respondText("Hello $name!")
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication(args)
}
```

Run application with `web-server` argument, then

```shell
curl http://localhost:8080/greeting
```

It will print `Hello World!`

**Auto testing**

```kotlin
// ApplicationTest.kt

class ApplicationTest {
    @Test
    fun `default greeting`() {
        webApplicationTest(KtorServerDiProfile::class) { client ->
            val response = client.get("/greeting")
            assertEquals("Hello World!",  response.bodyAsText())
        }
    }

    @Test
    fun `custom greeting`() {
        webApplicationTest(KtorServerDiProfile::class) { client ->
            val response = client.get("/greeting?name=Universe")
            assertEquals("Hello Universe!",  response.bodyAsText())
        }
    }
}
```

### GraphQL Server

[Source code](samples/graphql-server)

```kotlin
// build.gradle.kts

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.apollographql.apollo3")
}

repositories {
    maven {
        name = "HardKore"
        url = uri("https://maven.pkg.github.com/alon-sage/hardkore-framework")
    }
}

dependencies {
    kapt("com.google.auto.service:auto-service")

    implementation("com.github.alon-sage.hardkore:hardkore-graphql-server-starter:0.1.1")

    testImplementation("com.github.alon-sage.hardkore:hardkore-graphql-server-testing:0.1.1")
}

apollo {
    service("service") {
        schemaFiles.from("src/main/resources/schema.graphql")
        packageName.set("com.github.alonsage.hardkore.samples.graphqlserver")
        srcDir("src/test/graphql")
        outputDirConnection {
            connectToKotlinSourceSet("test")
        }
        generateOptionalOperationVariables = false
    }
}
```

```graphql
# src/main/resources/schema.graphql

type Query {
    user(id: ID): User
}

type Subscription {
    userEvents(userId: ID): Event
}

type User {
    id: ID
    name: String
    lastEvents(number: Int): [Event]
}

type Event {
    id: ID
    message: String
}
```

```kotlin
// Application.kt

@AutoService(DiModule::class)
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
    suspend fun Query.user(id: UUID): User =
        User(id, "foo")

    suspend fun User.lastEvents(number: Int): List<Event> =
        List(number) { Event(UUID.randomUUID(), "event-$it") }

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
    runApplication(args)
}
```

Run application with `graphql-server` argument, then

```shell
curl -X POST \
  -H 'Content-Type: application/json' \
  -d '{"query": "query { user(id: \"b13c7592-66de-405c-9fe5-98a8187a2760\") { id name lastEvents(number: 3) { id message } } }"}' \
  http://localhost:8080/graphql
```

It will print something like this

```json
{
    "data": {
        "user": {
            "id": "b13c7592-66de-405c-9fe5-98a8187a2760",
            "name": "foo",
            "lastEvents": [
                {
                    "id": "d31f3978-0efc-475b-a14a-5915a7e7ce94",
                    "message": "event-0"
                },
                {
                    "id": "2b3a8713-9b78-40d5-a9a7-fad98c5f2064",
                    "message": "event-1"
                },
                {
                    "id": "b84fefcb-56c3-45d0-bee1-877db896d72d",
                    "message": "event-2"
                }
            ]
        }
    }
}
```

**Auto testing**

```graphql
# src/test/graphql/operations.graphql

query GetUserWithEvents($userId: ID, $eventsNumber: Int) {
    user(id: $userId) {
        id
        name
        lastEvents(number: $eventsNumber) {
            id
            message
        }
    }
}

subscription WatchEvents($userId: ID) {
    userEvents(userId: $userId) {
        id
        message
    }
}
```

```kotlin
// ApplicationTest.kt

class ApplicationTest {
    @Test
    fun `returns user with events`() {
        webApplicationTest(GraphQLServerDiProfile::class) { client ->
            val query = GetUserWithEventsQuery(
                userId = UUID.randomUUID().toString(),
                eventsNumber = 3
            )
            val data = client.graphql().query(query).execute().let { result ->
                assertNull(result.exception)
                assertNull(result.errors)
                assertNotNull(result.data)
            }
            assertNotNull(data.user) { user ->
                assertEquals(query.userId, user.id)
                assertEquals(query.eventsNumber, user.lastEvents?.size)
            }
        }
    }

    @Test
    fun `returns watched events`() {
        webApplicationTest(GraphQLServerDiProfile::class) { baseClient ->
            val client = baseClient.config { install(WebSockets) }
            val subscription = WatchEventsSubscription(userId = UUID.randomUUID().toString())
            val results = client.graphql().subscription(subscription).toFlow().take(10).toList()
            results.forEach { result ->
                assertNull(result.exception)
                assertNull(result.errors)
                assertNotNull(result.data) { data ->
                    assertNotNull(data.userEvents) { event ->
                        assertContains(event.message!!, subscription.userId!!)
                    }
                }
            }
        }
    }
}
```

## Packages:

* [hardkore](hardkore/README.md) - implements dependency injection, configuration, lifecycles,
  observability, and some useful utilities


* [hardkore-ktor-server](hardkore-ktor-server/README.md) - a Ktor server integration
* [hardkore-ktor-server-starter](hardkore-ktor-server-starter/README.md) - a small wrapper that automatically registers
  Ktor server CLI command
* [hardkore-ktor-server-testing](hardkore-ktor-server-testing/README.md) - implements utility to test server


* [hardkore-graphql-server](hardkore-graphql-server/README.md) - a GraphQL server implementation on top of Ktor
* [hardkore-graphql-server-starter](hardkore-graphql-server-starter/README.md) - a small wrapper that automatically
  registers GraphQL server CLI command
* [hardkore-graphql-server-testing](hardkore-graphql-server-testing/README.md) - implements utility to test server