package net.apemon.poc.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

data class PrivateMessageState(val from: Party,
                               val to: Party,
                               val type: String,
                               val message: String,
                               val hash: String,
                               override val linearId: UniqueIdentifier = UniqueIdentifier()):LinearState {
    override val participants: List<AbstractParty>
        get() = listOf(from, to)
}