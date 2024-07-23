plugins {
    `java-library`
    kotlin("jvm")
    id("publish-artifacts")
}

description = """
    HardKore small wrapper that automatically registers GraphQL server CLI command
""".trimIndent()

dependencies {
    api(project(":hardkore-graphql-server"))
}