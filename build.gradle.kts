import org.jetbrains.kotlin.com.github.gundy.semver4j.model.Version
import org.jetbrains.kotlin.com.github.gundy.semver4j.model.Version.Identifier
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

    id("com.palantir.git-version") version "3.1.0"
}

group = "com.github.alon-sage.hardkore"
version = gitVersion()

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

fun gitVersion(): String {
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    val tagVersion = Version.fromString(details.lastTag.removePrefix("v"))
    return Version.builder()
        .major(tagVersion.major)
        .minor(tagVersion.minor)
        .patch(tagVersion.patch)
        .preReleaseIdentifiers(
            buildList<Identifier> {
                addAll(tagVersion.preReleaseIdentifiers)

                if (details.branchName == null || details.branchName == "main") {
                    if (details.commitDistance > 0) {
                        add(Identifier.fromString(details.gitHashFull.substring(0, 8)))
                    }
                } else {
                    add(Identifier.fromString("SNAPSHOT"))
                    add(Identifier.fromString(details.gitHashFull.substring(0, 8)))
                    if (details.branchName != "dev") {
                        val sanitizedBranchName = details.branchName.replace("\\W+".toRegex(), "-")
                        add(Identifier.fromString(sanitizedBranchName))
                    }
                }

                if (details.version.endsWith(".dirty")) {
                    add(Identifier.fromString("dirty"))
                }
            }
        )
        .buildIdentifiers(
            buildList<Identifier> {
                if (details.commitDistance > 0) {
                    add(Identifier.fromString(details.commitDistance.toString()))
                }
            }
        )
        .build()
        .toString()
}