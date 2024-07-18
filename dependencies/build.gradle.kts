import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

plugins {
    `java-platform`
    id("publish-artifacts")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("org.jetbrains.kotlin:kotlin-bom:${getKotlinPluginVersion()}"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.8.1"))
    api(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.6.3"))
    api(platform("org.apache.logging.log4j:log4j-bom:2.23.1"))
    api(platform("io.opentelemetry:opentelemetry-bom:1.39.0"))
    api(platform("org.junit:junit-bom:5.10.2"))
    api(platform("io.ktor:ktor-bom:2.3.11"))

    constraints {
        api("com.google.auto.service:auto-service:1.1.1")
        api("com.google.auto.service:auto-service-annotations:1.1.1")

        api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

        api("com.typesafe:config:1.4.3")

        api("org.slf4j:slf4j-api:2.0.13")
        api("org.slf4j:slf4j-simple:2.0.13")
        api("org.slf4j:jul-to-slf4j:2.0.13")
        api("ch.qos.logback:logback-core:1.5.6")
        api("ch.qos.logback:logback-classic:1.5.6")

        api("com.github.ajalt.clikt:clikt:4.4.0")

        api("com.graphql-java:graphql-java:22.1")
        api("com.graphql-java:graphql-java-extended-scalars:22.0")
        api("com.apollographql.federation:federation-graphql-java-support:5.1.0")

        api("com.apollographql.apollo3:apollo-runtime:4.0.0-beta.7")
        api("com.apollographql.apollo3:apollo-engine-ktor:4.0.0-beta.7")

        api("io.mockk:mockk:1.13.8")
    }
}