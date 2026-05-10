package com.example.wschat.controller

import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal

@Controller
class ChatController(
    private val messagingTemplate: SimpMessageSendingOperations,
    private val userRegistry: SimpUserRegistry
) {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    fun sendMessage(@Payload message: ChatMessage): ChatMessage {
        require(message.content.length <= 500) { "Message too long (max 500 chars)" }
        return message
    }

    @MessageMapping("/private")
    fun sendPrivateMessage(@Payload message: PrivateMessage, principal: Principal) {
        messagingTemplate.convertAndSendToUser(
            message.recipient,
            "/queue/private",
            ChatMessage(sender = principal.name, content = message.content, type = MessageType.CHAT)
        )
    }

    @GetMapping("/users/online")
    @ResponseBody
    fun getOnlineUsers(): List<String> {
        return userRegistry.users.map { it.name }
    }
}

data class PrivateMessage(val recipient: String, val content: String)
