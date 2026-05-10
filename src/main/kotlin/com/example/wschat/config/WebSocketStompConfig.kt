package com.example.wschat.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration

@Configuration
@EnableWebSocketMessageBroker
class WebSocketStompConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        val taskScheduler = ThreadPoolTaskScheduler().apply {
            poolSize = 1
            setThreadNamePrefix("ws-heartbeat-")
            initialize()
        }
        registry.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(longArrayOf(10000, 10000))
            .setTaskScheduler(taskScheduler)
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/chat")
            .setAllowedOriginPatterns("*")
            .addInterceptors(UserHandshakeInterceptor())
            .setHandshakeHandler(UserPrincipalHandshakeHandler())
            .withSockJS()
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry
            .setMessageSizeLimit(8 * 1024)
            .setSendBufferSizeLimit(512 * 1024)
            .setSendTimeLimit(20000)
    }
}
