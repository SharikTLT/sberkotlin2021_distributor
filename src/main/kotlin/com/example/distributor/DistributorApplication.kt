package com.example.distributor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener

@SpringBootApplication
class DistributorApplication

fun main(args: Array<String>) {
    runApplication<DistributorApplication>(*args)
}


