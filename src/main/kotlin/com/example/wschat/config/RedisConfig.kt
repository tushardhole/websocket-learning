package com.example.wschat.config

import com.example.wschat.pubsub.RedisMessageSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(connectionFactory)
    }

    @Bean
    fun chatTopic(): ChannelTopic = ChannelTopic("chat:messages")

    @Bean
    fun privateTopic(): ChannelTopic = ChannelTopic("chat:private")

    @Bean
    fun messageListenerAdapter(subscriber: RedisMessageSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(subscriber, "onMessage")
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        subscriber: RedisMessageSubscriber
    ): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(connectionFactory)
        container.addMessageListener(
            MessageListenerAdapter(subscriber, "onPublicMessage"),
            chatTopic()
        )
        container.addMessageListener(
            MessageListenerAdapter(subscriber, "onPrivateMessage"),
            privateTopic()
        )
        return container
    }
}
