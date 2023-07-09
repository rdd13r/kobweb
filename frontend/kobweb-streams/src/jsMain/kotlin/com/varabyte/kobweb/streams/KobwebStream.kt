package com.varabyte.kobweb.streams

import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket

interface StreamListener {
    fun onOpen()
    fun onClose()
    fun onMessage(message: StreamMessage)
}

sealed interface StreamEvent {
    object Opened : StreamEvent
    object Closed : StreamEvent
    class Text(val text: String) : StreamEvent
}

class KobwebStream(val route: String) {
    internal class WebSocketChannel {
        private var isOpen = false

        private val listeners = mutableListOf<StreamListener>()

        private val socket = run {
            val location = window.location
            val scheme = if (location.protocol == "https:") "wss" else "ws";
            val webSocketUrl = scheme + "://" + location.host + "/kobweb-streams";
            WebSocket(webSocketUrl)
        }

        fun addListener(listener: StreamListener) {
            listeners.add(listener)
            if (isOpen) {
                listener.onOpen()
            }
        }

        fun removeListener(listener: StreamListener) {
            listeners.remove(listener)
            if (isOpen) {
                // We're killing a specific stream before the overall websocket was killed. Let's
                // just act like the websocket closed in that case, so we run any shutdown logic.
                listener.onClose()
            }
        }

        fun send(message: StreamMessage) {
            socket.send(Json.encodeToString(message))
        }

        init {
            socket.onopen = {
                isOpen = true
                listeners.forEach { it.onOpen() }
            }

            socket.onclose = {
                isOpen = false
                listeners.forEach { it.onClose() }
                listeners.clear()
            }

            socket.onmessage = { event ->
                val message = Json.decodeFromString<StreamMessage>(event.data.toString())
                listeners.forEach { it.onMessage(message) }
            }
        }

        fun close() {
            socket.close()
        }
    }

    class StreamScope internal constructor(
        private val channel: WebSocketChannel,
        private val stream: KobwebStream
    ) {
        internal var isClosed = false
            private set

        fun send(text: String) {
            channel.send(StreamMessage(stream.route, text))
        }

        fun close() {
            isClosed = true
        }
    }

    companion object {
        private var _channel: WebSocketChannel? = null
        private var activeStreamCount = 0

        private fun connectChannel(): WebSocketChannel {
            if (activeStreamCount == 0) {
                _channel = WebSocketChannel()
            }
            ++activeStreamCount
            return _channel!!
        }

        private fun disconnectChannel() {
            check(activeStreamCount > 0) { "Called disconnectChannel more often than connectChannel"}
            --activeStreamCount
            if (activeStreamCount == 0) {
                _channel!!.close()
                _channel = null
            }
        }

    }

    private var channel: WebSocketChannel? = null
    private val isClosed = CompletableDeferred<Unit>()

    suspend fun connect(handleEvent: StreamScope.(StreamEvent) -> Unit) {
        val channel = connectChannel()
        this.channel = channel

        val scope = StreamScope(channel, this)

        val listener = object : StreamListener {
            override fun onOpen() {
                scope.handleEvent(StreamEvent.Opened)
                if (scope.isClosed) {
                    onClose()
                } else {
                    enqueuedMessages.forEach { message ->
                        channel.send(StreamMessage(route, message))
                    }
                }
            }

            override fun onClose() {
                scope.handleEvent(StreamEvent.Closed)
                isClosed.complete(Unit)
            }

            override fun onMessage(message: StreamMessage) {
                // We have one websocket that can traffic multiple streams. If we're connected for multiple streams,
                // we'll get messages for all of them. Only respond to the client stream we are associated with.
                if (message.route != route) return

                scope.handleEvent(StreamEvent.Text(message.payload))
                if (scope.isClosed) {
                    onClose()
                }
            }
        }

        channel.addListener(listener)
        isClosed.await()
        channel.removeListener(listener)
        disconnectChannel()
        this.channel = null
    }

    private val enqueuedMessages = mutableListOf<String>()
    enum class IfSentBeforeConnectedStrategy {
        /**
         * If the stream is not connected at the time we want to send a message, queue it up to be sent when it is.
         *
         * If multiple messages were enqueued (and not cleared by [CLEAR_PREVIOUS]), they will be sent sequentially as
         * soon as this stream connects.
         */
        ENQUEUE,

        /**
         * If the stream is not connected at the time we want to send a message, clear any previously enqueued messages
         * and only send this one when the stream connects.
         *
         * This can be useful if you want to send a message that is only relevant to the most recent state of your site.
         */
        CLEAR_PREVIOUS,

        /**
         * If the stream is not connected at the time we want to send a message, do nothing.
         */
        SKIP,
    }

    fun send(text: String, strategy: IfSentBeforeConnectedStrategy = IfSentBeforeConnectedStrategy.ENQUEUE) {
        val channel = channel
        if (channel == null) {
            when (strategy) {
                IfSentBeforeConnectedStrategy.ENQUEUE -> enqueuedMessages.add(text)
                IfSentBeforeConnectedStrategy.CLEAR_PREVIOUS -> { enqueuedMessages.clear(); enqueuedMessages.add(text) }
                IfSentBeforeConnectedStrategy.SKIP -> {}
            }
        } else {
            channel.send(StreamMessage(route, text))
        }
    }

    fun disconnect() {
        isClosed.complete(Unit)
    }
}
