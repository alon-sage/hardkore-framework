import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "17"
        allWarningsAsErrors = true
    }
}