package com.github.alonsage.hardkore.graphql.server

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.runtime.RunServicesApplicationCommand

class GraphQLServerCommand : RunServicesApplicationCommand(
    name = "graphql-server",
    help = "Runs application GraphQL server"
) {
    override fun Binder.setup() {
        autoInstall(GraphQLServerDiProfile::class)
    }
}