plugins {
    `java-library`
    kotlin("jvm")
    id("publish-artifacts")
}

description = """
    HardKore utils to test Ktor server
""".trimIndent()

dependencies {
    api(project(":hardkore-ktor-server"))
    api("io.ktor:ktor-client-core")

    implementation("io.ktor:ktor-server-test-host")
}