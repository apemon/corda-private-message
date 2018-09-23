package net.apemon.poc.flow

import co.paralleluniverse.fibers.Suspendable
import net.apemon.poc.contract.PublicMessageContract
import net.apemon.poc.state.PublicMessageState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import org.hibernate.Transaction

@InitiatingFlow
@StartableByRPC
class SendPublicMessageFlow(val identifier: UniqueIdentifier,
                            val issuer: Party,
                            val hash: String): FlowLogic<SignedTransaction>() {

    @Suspendable
    override fun call(): SignedTransaction {
        // get notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // create state
        val state = PublicMessageState(issuer,identifier,hash)
        // create command
        val signer = state.participants.map { it.owningKey }
        val issueCommand = Command(PublicMessageContract.Commands.Issue(), signer)
        // create transaction builder
        val builder = TransactionBuilder(notary = notary)
        builder.addOutputState(state, PublicMessageContract.PUBLIC_MESSAGE_CONTRACT_ID)
        builder.addCommand(issueCommand)
        // verify transaction
        builder.verify(serviceHub)
        // begin transaction
        val ptx = serviceHub.signInitialTransaction(builder)
        val ftx = subFlow(FinalityFlow(ptx))
        // broadcast transaction
        subFlow(BroadcastTransaction(ftx))
        return ftx
    }
}