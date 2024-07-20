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

* [hardcore](hardkore/README.md) - implements dependency injection, configuration, lifecycles,
  observability, and some useful utilities


* [hardcore-ktor-server](hardcore-ktor-server/README.md) - a Ktor server integration
* [hardcore-ktor-server-starter](hardcore-ktor-server-starter/README.md) - a small wrapper that automatically registers
  Ktor server CLI command
* [hardcore-ktor-server-testing](hardcore-ktor-server-testing/README.md) - implements utility to test server


* [hardcore-graphql-server](hardcore-graphql-server/README.md) - a GraphQL server implementation on top of Ktor
* [hardcore-graphql-server-starter](hardcore-graphql-server-starter/README.md) - a small wrapper that automatically
  registers GraphQL server CLI command
* [hardcore-graphql-server-testing](hardcore-graphql-server-testing/README.md) - implements utility to test server