package com.example.wschat.interceptor

import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class RateLimitInterceptor : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)
    private val userMessageCounts = ConcurrentHashMap<String, TokenBucket>()

    companion object {
        const val MAX_MESSAGES_PER_SECOND = 5
        const val BUCKET_CAPACITY = 10
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command != StompCommand.SEND) return message

        val username = accessor.user?.name ?: return message
        val bucket = userMessageCounts.computeIfAbsent(username) { TokenBucket(BUCKET_CAPACITY, MAX_MESSAGES_PER_SECOND) }

        if (!bucket.tryConsume()) {
            log.warn("Rate limit exceeded for user $username")
            throw IllegalStateException("Rate limit exceeded: max $MAX_MESSAGES_PER_SECOND messages/sec")
        }

        return message
    }
}

class TokenBucket(private val capacity: Int, private val refillRate: Int) {
    private var tokens = AtomicInteger(capacity)
    @Volatile private var lastRefillTime = System.currentTimeMillis()

    @Synchronized
    fun tryConsume(): Boolean {
        refill()
        return if (tokens.get() > 0) {
            tokens.decrementAndGet()
            true
        } else {
            false
        }
    }

    private fun refill() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRefillTime
        val tokensToAdd = (elapsed * refillRate / 1000).toInt()
        if (tokensToAdd > 0) {
            tokens.set(minOf(capacity, tokens.get() + tokensToAdd))
            lastRefillTime = now
        }
    }
}
