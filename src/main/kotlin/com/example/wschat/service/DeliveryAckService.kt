package com.example.wschat.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class DeliveryAckService(
    private val redisTemplate: StringRedisTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val ACK_PREFIX = "msg:ack:"
        private const val PENDING_PREFIX = "msg:pending:"
        private val PENDING_TTL = Duration.ofHours(24)
    }

    fun markPending(messageId: Long, recipient: String) {
        redisTemplate.opsForSet().add("$PENDING_PREFIX$recipient", messageId.toString())
        redisTemplate.expire("$PENDING_PREFIX$recipient", PENDING_TTL)
    }

    fun acknowledge(messageId: Long, username: String) {
        redisTemplate.opsForSet().remove("$PENDING_PREFIX$username", messageId.toString())
        redisTemplate.opsForValue().set(
            "$ACK_PREFIX$messageId:$username", "ACK", PENDING_TTL
        )
        log.debug("Message $messageId acknowledged by $username")
    }

    fun isAcknowledged(messageId: Long, username: String): Boolean {
        return redisTemplate.hasKey("$ACK_PREFIX$messageId:$username")
    }

    fun getPendingMessages(username: String): Set<String> {
        return redisTemplate.opsForSet().members("$PENDING_PREFIX$username") ?: emptySet()
    }

    fun isDuplicate(messageId: Long, username: String): Boolean {
        return isAcknowledged(messageId, username)
    }
}
