package net.apemon.poc.contract

import net.apemon.poc.state.PrivateMessageState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class PrivateMessageContract: Contract {
    companion object {
        @JvmStatic
        val PRIVATE_MESSAGE_CONTRACT_ID = "net.apemon.poc.contract.PrivateMessageContract"
    }

    interface Commands: CommandData {
        class Send: TypeOnlyCommandData(), Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<PrivateMessageContract.Commands>()
        when(command.value) {
            is Commands.Send -> requireThat {
                "No inputs should be consumed when send a new private message" using (tx.inputs.isEmpty())
                "Only one output state should be created" using (tx.outputs.size == 1)
                val privateMessage = tx.outputStates.single() as PrivateMessageState
                "Message type cannot be null" using (!privateMessage.type.isNullOrEmpty())
                "Message cannot be null" using (!privateMessage.message.isNullOrEmpty())
                "Hash cannot be null" using (!privateMessage.hash.isNullOrEmpty())
                "You cannot send message to yourself" using (privateMessage.to != privateMessage.from)
                "Sender and receiver must sign transaction" using (command.signers.toSet() == privateMessage.participants.map { it.owningKey }.toSet())
            }
        }
    }
}