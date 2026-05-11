package com.example.wschat.security

import com.example.wschat.config.StompPrincipal
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class StompChannelInterceptor : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        when (accessor.command) {
            StompCommand.SUBSCRIBE -> {
                val destination = accessor.destination ?: return message
                val principal = accessor.user as? StompPrincipal

                if (destination.startsWith("/topic/admin") && principal?.role != "ADMIN") {
                    log.warn("User ${principal?.name} denied access to $destination (not ADMIN)")
                    throw IllegalArgumentException("Access denied: admin topic requires ADMIN role")
                }
            }
            else -> {}
        }

        return message
    }
}
