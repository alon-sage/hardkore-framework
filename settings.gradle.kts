rootProject.name = "hardkore-framework"

// Build plugins
includeBuild("build-plugins")

// Dependencies
include("dependencies")

// Libraries
include("hardkore")
include("hardkore-ktor-server")
include("hardkore-ktor-server-starter")
include("hardkore-ktor-server-testing")
include("hardkore-graphql-server")
include("hardkore-graphql-server-starter")
include("hardkore-graphql-server-testing")

// Samples
include("samples:web-server")
include("samples:graphql-server")