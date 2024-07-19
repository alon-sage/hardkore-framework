plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    id("kotlinx-atomicfu")
    id("publish-artifacts")
}

dependencies {
    kapt("com.google.auto.service:auto-service")

    api(kotlin("reflect"))
    api("com.google.auto.service:auto-service-annotations")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
    api("com.github.ajalt.clikt:clikt")
    api("com.typesafe:config")
    api("org.slf4j:slf4j-api")
    api("io.opentelemetry:opentelemetry-api")
    api("io.opentelemetry:opentelemetry-extension-kotlin")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable")
    implementation("ch.qos.logback:logback-core")
    implementation("ch.qos.logback:logback-classic")
    implementation("org.apache.logging.log4j:log4j-to-slf4j")
    implementation("org.slf4j:jul-to-slf4j")
}