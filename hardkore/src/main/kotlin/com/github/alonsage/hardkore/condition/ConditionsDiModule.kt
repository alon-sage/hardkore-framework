package com.github.alonsage.hardkore.condition

import com.github.alonsage.hardkore.di.Binder
import com.github.alonsage.hardkore.di.DiModule
import com.github.alonsage.hardkore.di.bindFactory
import com.google.auto.service.AutoService

@AutoService(DiModule::class)
class ConditionsDiModule : DiModule {
    override fun Binder.install() {
        bindFactory<Conditions> { DefaultConditions() }
    }
}