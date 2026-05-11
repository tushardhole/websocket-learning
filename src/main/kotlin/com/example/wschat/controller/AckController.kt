package com.example.wschat.controller

import com.example.wschat.service.DeliveryAckService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class AckController(
    private val deliveryAckService: DeliveryAckService
) {

    @MessageMapping("/ack")
    fun acknowledgeMessage(@Payload ack: AckPayload, principal: Principal) {
        deliveryAckService.acknowledge(ack.messageId, principal.name)
    }
}

data class AckPayload(val messageId: Long)
