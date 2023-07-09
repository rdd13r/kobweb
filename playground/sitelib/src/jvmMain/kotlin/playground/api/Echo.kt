package playground.api

import com.varabyte.kobweb.api.stream.ApiStream

val echo = object : ApiStream() {
    override suspend fun onUserConnected(ctx: UserConnectedContext) {
        ctx.logger.debug("User connected: ${ctx.streamerId}")
    }

    override suspend fun onMessageReceived(ctx: MessageReceivedContext) {
        ctx.stream.send("Echoed: ${ctx.message}")
        if (ctx.message == "byte") {
            ctx.stream.disconnect()
        }
    }

    override suspend fun onUserDisconnected(ctx: UserDisconnectedContext) {
        ctx.logger.debug("User disconnected: ${ctx.streamerId}")
    }
}

//val echo = ApiStream("echo") { evt ->
//    evt.stream.send("Echoed: ${evt.message}")
//}
