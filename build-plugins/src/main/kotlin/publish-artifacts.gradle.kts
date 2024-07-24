import org.jetbrains.kotlin.com.github.gundy.semver4j.model.Version

plugins {
    id("com.vanniktech.maven.publish")
}

afterEvaluate {
    tasks.withType<Jar>().findByName("jar")?.apply {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }

    mavenPublishing {
        if (isRelease()) {
            publishToMavenCentral(automaticRelease = true)
            signAllPublications()
        }

        pom {
            inceptionYear = "2024"
            name = "${project.group}:${project.name}"
            description = project.description
            url = "https://github.com/alon-sage/hardkore-framework/"
            scm {
                url = "https://github.com/alon-sage/hardkore-framework/"
                connection = "scm:git:git://github.com/alon-sage/hardkore-framework.git"
                developerConnection = "scm:git:ssh://git@github.com/alon-sage/hardkore-framework.git"
            }
            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                    distribution = "repo"
                }
            }
            developers {
                developer {
                    id = "alon-sage"
                    name = "Ivan Babintsev"
                    url = "https://github.com/alon-sage/"
                }
            }
        }
    }
}

fun isRelease(): Boolean =
    Version.fromString(version.toString()).let {
        it.preReleaseIdentifiers.isEmpty() && it.buildIdentifiers.isEmpty()
    }