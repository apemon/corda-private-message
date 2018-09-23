package net.apemon.poc.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

data class PublicMessageState(val issuer: Party,
                              val identifier: UniqueIdentifier,
                              val hash: String,
                              override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants: List<AbstractParty>
        get() = listOf(issuer)
}