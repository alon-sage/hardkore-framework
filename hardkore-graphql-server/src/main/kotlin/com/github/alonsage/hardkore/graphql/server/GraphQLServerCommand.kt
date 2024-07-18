package com.github.alonsage.hardkore.graphql.server

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.runtime.RunServicesCommand

class GraphQLServerCommand : RunServicesCommand(
    name = "graphql-server",
    help = "Runs application GraphQL server"
) {
    override fun Binder.setup() {
        autoInstall(GraphQLServerDiProfile::class)
    }
}