package net.apemon.poc.flow

import co.paralleluniverse.fibers.Suspendable
import net.apemon.poc.state.PrivateMessageState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction

@StartableByRPC
class SendPrivateAndPublicMessageFlow(val from: Party,
                                      val to: Party,
                                      val identifier: UniqueIdentifier,
                                      val type: String,
                                      val message: String): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        val hash = SecureHash.sha256(message).toString()
        val privateState = PrivateMessageState(from, to, type, message, hash, identifier)
        subFlow(SendPrivateMessageFlow(privateState))
        return subFlow(SendPublicMessageFlow(identifier, from, hash))
    }
}