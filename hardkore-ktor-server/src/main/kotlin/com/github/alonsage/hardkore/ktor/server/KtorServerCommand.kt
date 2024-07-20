package com.github.alonsage.hardkore.ktor.server

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.runtime.RunServicesApplicationCommand

class KtorServerCommand : RunServicesApplicationCommand(
    name = "web-server",
    help = "Runs application web server"
) {
    override fun Binder.setup() {
        autoInstall(KtorServerDiProfile::class)
    }
}