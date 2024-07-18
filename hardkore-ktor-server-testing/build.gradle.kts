plugins {
    kotlin("jvm")
    id("publish-artifacts")
}

dependencies {
    api(project(":hardkore-ktor-server"))

    implementation("io.ktor:ktor-server-test-host")
}