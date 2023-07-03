package com.varabyte.kobweb.server.api

import com.charleskorn.kaml.Yaml
import com.varabyte.kobweb.project.KobwebFolder
import com.varabyte.kobweb.project.io.KobwebWritableTextFile

class ServerRequestsFile(kobwebFolder: KobwebFolder) : KobwebWritableTextFile<ServerRequests>(
    kobwebFolder,
    "server/requests.yaml",
    serialize = { requests -> Yaml.default.encodeToString(ServerRequests.serializer(), requests) },
    deserialize = { text -> Yaml.default.decodeFromString(ServerRequests.serializer(), text) }
) {
    fun enqueueRequest(request: ServerRequest) {
        val currRequests = content
        content = ServerRequests((currRequests?.requests ?: emptyList()) + request)
    }

    fun removeRequests(): List<ServerRequest> {
        return content?.requests.also { content = null } ?: emptyList()
    }
}
