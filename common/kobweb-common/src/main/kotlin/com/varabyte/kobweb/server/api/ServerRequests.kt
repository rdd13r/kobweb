package com.varabyte.kobweb.server.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Keep all children classes even if they can be objects; we may update them later
@Suppress("CanSealedSubClassBeObject")
@Serializable
sealed class ServerRequest {
    @Serializable
    @SerialName("Stop")
    class Stop : ServerRequest()

    @Serializable
    @SerialName("IncrementVersion")
    class IncrementVersion : ServerRequest()

    @Serializable
    @SerialName("SetStatus")
    class SetStatus(val message: String, val isError: Boolean = false, var timeoutMs: Long? = null) : ServerRequest()

    @Serializable
    @SerialName("ClearStatus")
    class ClearStatus : ServerRequest()
}

@Serializable
class ServerRequests(
    val requests: List<ServerRequest>
)
