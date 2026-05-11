package com.example.wschat.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class WebSocketMetrics(
    private val meterRegistry: MeterRegistry,
    private val userRegistry: SimpUserRegistry
) {
    private val activeConnections = AtomicInteger(0)

    private val messagesReceived: Counter = Counter.builder("ws.messages.received")
        .description("Total WebSocket messages received")
        .register(meterRegistry)

    private val messagesSent: Counter = Counter.builder("ws.messages.sent")
        .description("Total WebSocket messages sent (broadcast)")
        .register(meterRegistry)

    private val messageProcessingTimer: Timer = Timer.builder("ws.message.processing.time")
        .description("Time to process a WebSocket message")
        .register(meterRegistry)

    private val rateLimitHits: Counter = Counter.builder("ws.ratelimit.hits")
        .description("Number of rate limit rejections")
        .register(meterRegistry)

    init {
        meterRegistry.gauge("ws.connections.active", activeConnections)
    }

    fun recordMessageReceived() = messagesReceived.increment()
    fun recordMessageSent() = messagesSent.increment()
    fun recordRateLimitHit() = rateLimitHits.increment()
    fun messageTimer(): Timer = messageProcessingTimer

    fun onConnect() = activeConnections.incrementAndGet()
    fun onDisconnect() = activeConnections.decrementAndGet()

    @Scheduled(fixedRate = 10000)
    fun syncConnectionCount() {
        activeConnections.set(userRegistry.userCount)
    }
}
