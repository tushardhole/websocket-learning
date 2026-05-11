package com.example.wschat.config

import com.example.wschat.security.JwtUtil
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

class UserHandshakeInterceptor(private val jwtUtil: JwtUtil) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        if (request is ServletServerHttpRequest) {
            val token = request.servletRequest.getParameter("token")
            if (token != null) {
                val username = jwtUtil.validateAndGetUsername(token)
                val role = jwtUtil.getRole(token)
                if (username != null) {
                    attributes["username"] = username
                    attributes["role"] = role ?: "USER"
                    return true
                }
            }
        }
        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {}
}
