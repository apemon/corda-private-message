package net.apemon.poc.contract

import net.apemon.poc.state.PublicMessageState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class PublicMessageContract: Contract {
    companion object {
        @JvmStatic
        val PUBLIC_MESSAGE_CONTRACT_ID = "net.apemon.poc.contract.PublicMessageContract"
    }

    interface Commands: CommandData {
        class Issue: TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<PublicMessageContract.Commands>()
        when (command.value) {
            is Commands.Issue -> requireThat {
                "No inputs should be consumed when send a new private message" using (tx.inputs.isEmpty())
                "Only one output state should be created" using (tx.outputs.size == 1)
                val publicMessage = tx.outputStates.single() as PublicMessageState
                "Identifier cannot be null" using (publicMessage.identifier != null)
                "Hash cannot be null" using (!publicMessage.hash.isNullOrEmpty())
                "Issuer must sign transaction" using (command.signers.toSet() == publicMessage.participants.map { it.owningKey }.toSet())
            }
        }
    }
}