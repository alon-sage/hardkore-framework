plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
    id("publish-artifacts")
}

description = """
    HardKore integration with a Ktor server
""".trimIndent()

dependencies {
    kapt("com.google.auto.service:auto-service")

    api(project(":hardkore"))
    api("io.ktor:ktor-server")

    implementation("io.ktor:ktor-server-netty")

    testImplementation("io.ktor:ktor-client-cio")
}