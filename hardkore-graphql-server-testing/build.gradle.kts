plugins {
    kotlin("jvm")
    id("publish-artifacts")
}

dependencies {
    api(project(":hardkore-graphql-server"))
    api(project(":hardkore-ktor-server-testing"))
    api("com.apollographql.apollo3:apollo-runtime")

    implementation("com.apollographql.apollo3:apollo-engine-ktor")
}