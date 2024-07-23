plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    // Enable processing @AutoService annotation
    kapt("com.google.auto.service:auto-service")

    implementation(project(":hardkore-ktor-server-starter"))

    testImplementation(project(":hardkore-ktor-server-testing"))
}