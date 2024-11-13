plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.apollographql.apollo3")
}

dependencies {
    kapt("com.google.auto.service:auto-service")

    implementation(project(":hardkore-graphql-server-starter"))

    testImplementation(project(":hardkore-graphql-server-testing"))
}

apollo {
    service("service") {
        schemaFiles.from("src/main/resources/schema.graphql")
        packageName.set("io.github.alonsage.hardkore.samples.graphqlserver")
        srcDir("src/test/graphql")
        outputDirConnection {
            connectToKotlinSourceSet("test")
        }
        generateOptionalOperationVariables = true
        mapScalarToUpload("File")
    }
}