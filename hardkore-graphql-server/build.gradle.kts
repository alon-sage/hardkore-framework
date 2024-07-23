plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("publish-artifacts")
}

description = """
    HardKore integration with a GraphQL Java
""".trimIndent()

dependencies {
    kapt("com.google.auto.service:auto-service")

    api(project(":hardkore-ktor-server"))
    api("io.ktor:ktor-server-auth")
    api("com.graphql-java:graphql-java")
    api("com.graphql-java:graphql-java-extended-scalars")

    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-websockets")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("com.apollographql.federation:federation-graphql-java-support")
}