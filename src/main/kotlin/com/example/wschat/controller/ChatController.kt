package com.example.wschat.controller

import com.example.wschat.model.ChatMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    fun sendMessage(message: ChatMessage): ChatMessage {
        return message
    }
}
