# HardKore

HardKore is a set of Kotlin libraries that helps develop and maintain backend applications. It covers configuration,
inversion of control, lifecycle, testing, and observability, and implements integration with some popular libraries like
Ktor and GraphQL Java.

HardKore built on the next principles:

* Intensive utilization of Kotlin language features
* First-class Kotlin coroutines support
* Modularization and extensibility
* Incorporating widely recognized libraries

Packages:

* [hardkore](hardkore/README.md) - implements dependency injection, configuration, lifecycles,
  observability, and some useful utilities


* [hardkore-ktor-server](hardkore-ktor-server/README.md) - a Ktor server integration
* [hardkore-ktor-server-starter](hardkore-ktor-server-starter/README.md) - a small wrapper that automatically registers
  Ktor server CLI command
* [hardkore-ktor-server-testing](hardkore-ktor-server-testing/README.md) - implements utility to test server


* [hardkore-graphql-server](hardkore-graphql-server/README.md) - a GraphQL server implementation on top of Ktor
* [hardkore-graphql-server-starter](hardkore-graphql-server-starter/README.md) - a small wrapper that automatically
  registers GraphQL server CLI command
* [hardkore-graphql-server-testing](hardkore-graphql-server-testing/README.md) - implements utility to test server