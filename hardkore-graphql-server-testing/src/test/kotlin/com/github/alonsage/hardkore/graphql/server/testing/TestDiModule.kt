package com.github.alonsage.hardkore.graphql.server.testing

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.DiProfiles
import com.github.alonsage.hardkore.graphql.server.GraphQLServerDiProfile
import com.github.alonsage.hardkore.graphql.server.dataclasses.bindGraphQLResolver
import com.google.auto.service.AutoService

@AutoService(DiModule::class)
@DiProfiles(GraphQLServerDiProfile::class)
class TestDiModule : DiModule {
    override fun Binder.install() {
        bindGraphQLResolver { TestResolver() }
    }
}