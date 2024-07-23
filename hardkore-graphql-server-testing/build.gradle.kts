plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    id("publish-artifacts")
    id("com.apollographql.apollo3")
}

description = """
    HardKore utils to test GraphQL server
""".trimIndent()

dependencies {
    api(project(":hardkore-graphql-server"))
    api(project(":hardkore-ktor-server-testing"))
    api("com.apollographql.apollo3:apollo-runtime")

    implementation("com.apollographql.apollo3:apollo-engine-ktor")

    kaptTest("com.google.auto.service:auto-service")
}

apollo {
    service("service") {
        schemaFiles.from("src/test/resources/schema.graphql")
        packageName.set("io.github.alonsage.hardkore.graphql.server.testing")
        srcDir("src/test/graphql")
        outputDirConnection {
            connectToKotlinSourceSet("test")
        }
        generateOptionalOperationVariables = false
    }
}
