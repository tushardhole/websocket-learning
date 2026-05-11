package com.example.wschat.pubsub

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisMessagePublisher(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publishPublicMessage(message: Any) {
        val json = objectMapper.writeValueAsString(message)
        log.debug("Publishing to chat:messages: $json")
        redisTemplate.convertAndSend("chat:messages", json)
    }

    fun publishPrivateMessage(envelope: PrivateEnvelope) {
        val json = objectMapper.writeValueAsString(envelope)
        log.debug("Publishing to chat:private: $json")
        redisTemplate.convertAndSend("chat:private", json)
    }
}

data class PrivateEnvelope(val recipient: String, val sender: String, val content: String)
