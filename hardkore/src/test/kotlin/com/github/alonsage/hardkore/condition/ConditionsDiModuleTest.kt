package com.github.alonsage.hardkore.condition

import com.github.alonsage.hardkore.di.DiContext
import com.github.alonsage.hardkore.di.bean
import org.junit.jupiter.api.Test

class ConditionsDiModuleTest {
    @Test
    fun `Conditions instance available`() {
        val context = DiContext { install(ConditionsDiModule()) }
        context.bean<Conditions>()
    }
}