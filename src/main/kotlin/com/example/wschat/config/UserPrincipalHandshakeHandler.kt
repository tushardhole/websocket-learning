package com.example.wschat.config

import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

class UserPrincipalHandshakeHandler : DefaultHandshakeHandler() {

    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal? {
        val username = attributes["username"] as? String ?: return null
        val role = attributes["role"] as? String ?: "USER"
        return StompPrincipal(username, role)
    }
}

data class StompPrincipal(private val username: String, val role: String) : Principal {
    override fun getName(): String = username
}
