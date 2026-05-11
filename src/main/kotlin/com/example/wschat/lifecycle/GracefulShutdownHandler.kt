package com.example.wschat.lifecycle

import com.example.wschat.model.ChatMessage
import com.example.wschat.model.MessageType
import com.example.wschat.session.RedisSessionRegistry
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.stereotype.Component

@Component
class GracefulShutdownHandler(
    private val messagingTemplate: SimpMessageSendingOperations,
    private val userRegistry: SimpUserRegistry,
    private val sessionRegistry: RedisSessionRegistry,
    @Value("\${app.instance-id:local}") private val instanceId: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PreDestroy
    fun onShutdown() {
        val connectedUsers = userRegistry.userCount
        log.info("Graceful shutdown initiated on $instanceId — notifying $connectedUsers connected users")

        messagingTemplate.convertAndSend(
            "/topic/system",
            ChatMessage(
                sender = "SYSTEM",
                content = "Server $instanceId is restarting. You will be reconnected automatically.",
                type = MessageType.SERVER_SHUTDOWN
            )
        )

        Thread.sleep(1000)

        sessionRegistry.cleanupInstance()
        log.info("Shutdown complete — sessions cleaned up, proceeding with termination")
    }
}
