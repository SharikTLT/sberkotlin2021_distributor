package com.example.distributor.config

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.DelegatingInvocableHandler
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod


@Configuration
class RabbitConfig {

    @Autowired
    lateinit var distributorConfig: DistributorConfig

    val log = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun queue(): Queue? {
        return Queue(distributorConfig.queue, false)
    }

    @Bean
    fun exchange(): TopicExchange? {
        return TopicExchange(distributorConfig.exchange)
    }

    @Bean
    fun binding(queue: Queue?, exchange: TopicExchange?): Binding? {
        return BindingBuilder.bind(queue).to(exchange).with(distributorConfig.inputKey)
    }

    @Bean
    fun container(
        connectionFactory: ConnectionFactory?,
        listenerAdapter: MessagingMessageListenerAdapter?
    ): DirectMessageListenerContainer? {
        val container = DirectMessageListenerContainer()
        container.connectionFactory = connectionFactory!!
        container.setQueueNames(distributorConfig.queue)
        container.setMessageListener(listenerAdapter!!)


        log.info("Build listener container")
        return container
    }

    @Bean
    fun listenerAdapter(receiver: ListenerAdapter?): MessagingMessageListenerAdapter? {
        val adapter = MessagingMessageListenerAdapter()
        adapter.setHandlerAdapter(HandlerAdapter(InvocableHandlerMethod(receiver!!, receiver!!.javaClass.getMethod("receive", Message::class.java))))
        return adapter
    }
}