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
    api("com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin:0.29.0")
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "17"
        allWarningsAsErrors = true
    }
}