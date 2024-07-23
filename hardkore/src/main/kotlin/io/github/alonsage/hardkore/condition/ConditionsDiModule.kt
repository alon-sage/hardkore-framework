package io.github.alonsage.hardkore.condition

import io.github.alonsage.hardkore.di.Binder
import io.github.alonsage.hardkore.di.DiModule
import io.github.alonsage.hardkore.di.bindFactory
import com.google.auto.service.AutoService

@AutoService(DiModule::class)
class ConditionsDiModule : DiModule {
    override fun Binder.install() {
        bindFactory<Conditions> { DefaultConditions() }
    }
}