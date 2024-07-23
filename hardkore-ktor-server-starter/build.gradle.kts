plugins {
    `java-library`
    kotlin("jvm")
    id("publish-artifacts")
}

description = """
    HardKore small wrapper that automatically registers Ktor server CLI command
""".trimIndent()

dependencies {
    api(project(":hardkore-ktor-server"))

    testImplementation("io.ktor:ktor-client-cio")
}