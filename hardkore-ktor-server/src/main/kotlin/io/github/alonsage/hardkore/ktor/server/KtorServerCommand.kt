package io.github.alonsage.hardkore.ktor.server

import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.runtime.RunServicesApplicationCommand

class KtorServerCommand : RunServicesApplicationCommand(
    name = "web-server",
    help = "Runs application web server"
) {
    override fun Binder.setup() {
        autoInstall(KtorServerDiProfile::class)
    }
}