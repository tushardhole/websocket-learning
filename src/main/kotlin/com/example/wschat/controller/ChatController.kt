package com.example.wschat.controller

import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import com.example.wschat.pubsub.PrivateEnvelope
import com.example.wschat.pubsub.RedisMessagePublisher
import com.example.wschat.service.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal

@Controller
class ChatController(
    private val userRegistry: SimpUserRegistry,
    private val chatService: ChatService,
    private val redisPublisher: RedisMessagePublisher
) {

    @MessageMapping("/chat")
    fun sendMessage(@Payload message: ChatMessage) {
        require(message.content.length <= 500) { "Message too long (max 500 chars)" }
        if (message.type == MessageType.CHAT) {
            chatService.savePublicMessage(message)
        }
        redisPublisher.publishPublicMessage(message)
    }

    @MessageMapping("/private")
    fun sendPrivateMessage(@Payload message: PrivateMessage, principal: Principal) {
        chatService.savePrivateMessage(principal.name, message.recipient, message.content)
        redisPublisher.publishPrivateMessage(
            PrivateEnvelope(
                recipient = message.recipient,
                sender = principal.name,
                content = message.content
            )
        )
    }

    @GetMapping("/users/online")
    @ResponseBody
    fun getOnlineUsers(): List<String> {
        return userRegistry.users.map { it.name }
    }

    @GetMapping("/messages/history")
    @ResponseBody
    fun getMessageHistory(@RequestParam(defaultValue = "50") limit: Int): List<ChatMessage> {
        return chatService.getRecentPublicMessages(limit.coerceAtMost(100))
    }
}

data class PrivateMessage(val recipient: String, val content: String)
