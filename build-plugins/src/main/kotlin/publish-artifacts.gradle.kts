plugins {
    `maven-publish`
}

afterEvaluate {
    extensions.findByType<JavaPluginExtension>()?.apply {
        withSourcesJar()
    }

    tasks.withType<Jar>().findByName("jar")?.apply {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }

    publishing {
        publications {
            create<MavenPublication>("Library") {
                components.findByName("java")?.let { from(it) }
                components.findByName("javaPlatform")?.let { from(it) }
                pom {
                    inceptionYear = "2024"
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

        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/alon-sage/hardkore-framework")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}