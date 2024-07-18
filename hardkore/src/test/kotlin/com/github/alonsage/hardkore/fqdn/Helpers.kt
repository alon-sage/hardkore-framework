package com.github.alonsage.hardkore.fqdn

import java.util.*

class RootFqdn(
    override val id: String
) : Fqdn.Root<String>()

class NestedFqdn(
    override val id: UUID,
    override val owner: RootFqdn
) : Fqdn.Nested<UUID, RootFqdn>()