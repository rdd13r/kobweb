package com.varabyte.kobweb.api

import com.varabyte.kobweb.api.data.Data
import com.varabyte.kobweb.api.http.Request
import com.varabyte.kobweb.api.http.Response
import com.varabyte.kobweb.api.log.Logger
import com.varabyte.kobweb.api.stream.ApiStream
import com.varabyte.kobweb.api.stream.StreamEvent

/**
 * The class which manages all API paths and handlers within a Kobweb project.
 */
@Suppress("unused") // Called by generated code
class Apis(private val data: Data, private val logger: Logger) {
    private val restHandlers = mutableMapOf<String, suspend (ApiContext) -> Unit>()
    private val streamHandlers = mutableMapOf<String, ApiStream>()

    fun register(path: String, handler: suspend (ApiContext) -> Unit) {
        restHandlers[path] = handler
    }

    fun registerStream(path: String, streamHandler: ApiStream) {
        streamHandlers[path.removePrefix("/")] = streamHandler
    }

    suspend fun handle(path: String, request: Request): Response? {
        return restHandlers[path]?.let { handler ->
            val apiCtx = ApiContext(request, data, logger)
            handler.invoke(apiCtx)
            apiCtx.res
        }
    }

    suspend fun handle(path: String, event: StreamEvent) {
        streamHandlers[path.removePrefix("/")]?.let { streamHandler ->
            when (event) {
                is StreamEvent.Opened -> {
                    streamHandler.onUserConnected(
                        ApiStream.UserConnectedContext(
                            event.stream,
                            event.streamerId,
                            data,
                            logger
                        )
                    )
                }

                is StreamEvent.Text -> {
                    streamHandler.onMessageReceived(
                        ApiStream.MessageReceivedContext(
                            event.stream,
                            event.streamerId,
                            event.text,
                            data,
                            logger
                        )
                    )
                }

                is StreamEvent.Closed -> {
                    streamHandler.onUserDisconnected(ApiStream.UserDisconnectedContext(event.streamerId, data, logger))
                }
            }
        }
    }
}
