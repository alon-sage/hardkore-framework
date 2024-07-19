plugins {
    `java-library`
    kotlin("jvm")
    id("publish-artifacts")
}

dependencies {
    api(project(":hardkore-graphql-server"))
}