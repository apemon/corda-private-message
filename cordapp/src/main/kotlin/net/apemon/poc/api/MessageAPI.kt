package net.apemon.poc.api

import net.apemon.poc.flow.SendPrivateAndPublicMessageFlow
import net.apemon.poc.flow.SendPrivateMessageFlow
import net.apemon.poc.flow.SendPublicMessageFlow
import net.apemon.poc.state.PrivateMessageState
import net.apemon.poc.state.PublicMessageState
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.crypto.SecureHash.SHA256
import net.corda.core.internal.x500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.messaging.vaultTrackBy
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("message")
class MessageAPI(val rpcOps: CordaRPCOps) {

    private val me = rpcOps.nodeInfo().legalIdentities.first().name;

    fun X500Name.toDisplayString() : String  = BCStyle.INSTANCE.toString(this)

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to me.x500Name.toDisplayString())

    @GET
    @Path("privateMessage")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPrivateMessage(): List<StateAndRef<ContractState>> {
        return rpcOps.vaultQueryBy<PrivateMessageState>().states
    }

    @GET
    @Path("publicMessage")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPublicMessage(): List<StateAndRef<ContractState>> {
        return rpcOps.vaultQueryBy<PublicMessageState>().states
    }

    @POST
    @Path("sendPrivate")
    fun sendPrivateMessage(request: sendPrivateMessageRequest): Response {
        try {
            val hash = SecureHash.sha256(request.message).toString()
            val sender = rpcOps.nodeInfo().legalIdentities.first()
            val receiver = rpcOps.partiesFromName(request.to, true).first()
            val state = PrivateMessageState(
                sender, receiver, request.type, request.message, hash
            )
            val trx = rpcOps.startFlow(::SendPrivateMessageFlow, state).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity(trx.tx.outputs.single().data.toString())
                    .build()
        } catch (e:Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    @POST
    @Path("sendPublic")
    fun sendPublcMessage(request: sendPublicMessageRequest): Response {
        try {
            val hash = request.hash
            val issuer = rpcOps.nodeInfo().legalIdentities.first()
            val identifier = UniqueIdentifier.fromString(request.identifier)
            val trx = rpcOps.startFlow(::SendPublicMessageFlow, identifier, issuer, hash).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity(trx.tx.outputs.single().data.toString())
                    .build()
        } catch (e:Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    @POST
    @Path("sendAll")
    fun sendAllMessage(request: sendPrivateMessageRequest): Response {
        try {
            val sender = rpcOps.nodeInfo().legalIdentities.first()
            val receiver = rpcOps.partiesFromName(request.to, true).first()
            val identifier = UniqueIdentifier()
            val trx = rpcOps.startFlow(::SendPrivateAndPublicMessageFlow, sender, receiver, identifier, request.type, request.message).returnValue.get()
            return Response
                    .status(Response.Status.OK)
                    .entity(trx.tx.outputs.single().data.toString())
                    .build()
        } catch (e:Exception) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.printStackTrace())
                    .build()
        }
    }

    data class sendPrivateMessageRequest(
            val to: String,
            val type: String,
            val message: String
    )

    data class sendPublicMessageRequest (
            val identifier: String,
            val hash: String
    )
}