package io.github.alonsage.hardkore.graphql.server.testing

import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DiModule
import io.github.alonsage.hardkore.di.DiProfiles
import io.github.alonsage.hardkore.graphql.server.GraphQLServerDiProfile
import io.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLResolver
import com.google.auto.service.AutoService

@AutoService(DiModule::class)
@DiProfiles(GraphQLServerDiProfile::class)
class TestDiModule : DiModule {
    override fun Binder.install() {
        bindGraphQLResolver { TestResolver() }
    }
}