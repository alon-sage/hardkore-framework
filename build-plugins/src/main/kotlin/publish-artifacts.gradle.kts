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