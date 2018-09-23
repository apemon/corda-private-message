package net.apemon.poc.client

import net.apemon.poc.state.PrivateMessageState
import net.corda.client.rpc.CordaRPCClient
import net.corda.core.contracts.StateAndRef
import net.corda.core.messaging.vaultTrackBy
import net.corda.core.utilities.loggerFor
import net.corda.core.utilities.NetworkHostAndPort.Companion.parse
import org.slf4j.Logger

fun main(args: Array<String>) = Client().main(args)

class Client {

    companion object {
        val logger: Logger = loggerFor<Client>()
        private fun logState(state: StateAndRef<PrivateMessageState>) = logger.info("{}", state.state.data)
    }

    fun main(args: Array<String>) {
        val nodeAddress = parse(args[0])
        val client = CordaRPCClient(nodeAddress);
        val proxy = client.start("user1","password").proxy
        val updates = proxy.vaultTrackBy<PrivateMessageState>().updates
        updates.toBlocking().subscribe { updates ->
            updates.produced.forEach { logState(it) }
        }
    }
}