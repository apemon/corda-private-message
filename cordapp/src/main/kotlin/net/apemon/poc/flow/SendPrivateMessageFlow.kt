package net.apemon.poc.flow

import co.paralleluniverse.fibers.Suspendable
import net.apemon.poc.contract.PrivateMessageContract
import net.apemon.poc.state.PrivateMessageState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

@InitiatingFlow
@StartableByRPC
class SendPrivateMessageFlow(val state: PrivateMessageState): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // get notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // get command
        val sendCommand = Command(PrivateMessageContract.Commands.Send(), state.participants.map { it.owningKey })
        // build transaction
        val builder = TransactionBuilder(notary = notary)
        builder.addOutputState(state, PrivateMessageContract.PRIVATE_MESSAGE_CONTRACT_ID)
        builder.addCommand(sendCommand)
        builder.verify(serviceHub)
        // initialate transaction
        val ptx = serviceHub.signInitialTransaction(builder)
        // send to counter party to sign
        val counterPartySession = initiateFlow(state.to)
        val stx = subFlow(CollectSignaturesFlow(ptx, listOf(counterPartySession)))
        val ftx = subFlow(FinalityFlow(stx))
        return ftx
    }
}

@InitiatedBy(SendPrivateMessageFlow::class)
class SendPrivateMessageFlowResponder(val flowSession: FlowSession): FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val signedTransactionFlow = object: SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) {

            }
        }
        subFlow(signedTransactionFlow)
    }
}