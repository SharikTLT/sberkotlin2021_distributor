package com.example.distributor.config

import org.springframework.amqp.core.Message

interface ListenerAdapter {
    fun receive(message: Message)
}