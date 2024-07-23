package io.github.alonsage.hardkore.fqdn

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class FqdnNodeName(val value: String)