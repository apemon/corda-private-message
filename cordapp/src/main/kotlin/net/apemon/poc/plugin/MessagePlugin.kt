package net.apemon.poc.plugin

import net.apemon.poc.api.MessageAPI
import net.corda.core.messaging.CordaRPCOps
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class MessagePlugin: WebServerPluginRegistry {
    override val webApis: List<Function<CordaRPCOps, out Any>>
        = listOf(Function(::MessageAPI))

    override val staticServeDirs: Map<String, String>
        = mapOf("web" to javaClass.classLoader.getResource("templateWeb").toExternalForm())
}