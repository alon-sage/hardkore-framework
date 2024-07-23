plugins {
    `java-library`
    kotlin("jvm")
    id("publish-artifacts")
}

dependencies {
    api(project(":hardkore-ktor-server"))
    api("io.ktor:ktor-client-core")

    implementation("io.ktor:ktor-server-test-host")
}