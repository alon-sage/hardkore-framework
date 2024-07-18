import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.22.0")
    }
}

plugins {
    val kotlinVersion = "1.9.23"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("kapt") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false

    id("com.apollographql.apollo3") version "4.0.0-beta.7" apply false
}


group = "com.github.alon-sage.hardkore"
version = "1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    afterEvaluate {
        if ("implementation" in configurations.names) dependencies {
            add("implementation", platform(project(":dependencies")))
        }

        if ("kapt" in configurations.names) dependencies {
            add("kapt", platform(project(":dependencies")))
        }

        if ("kaptTest" in configurations.names) dependencies {
            add("kaptTest", platform(project(":dependencies")))
        }

        if ("testImplementation" in configurations.names) dependencies {
            add("testImplementation", kotlin("test-junit5"))
            add("testRuntimeOnly", kotlin("reflect"))
            add("testImplementation", "io.mockk:mockk")
        }
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "17"
            allWarningsAsErrors = false
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        reports {
            junitXml.required.set(true)
        }
    }
}