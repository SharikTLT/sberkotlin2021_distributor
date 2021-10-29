package com.example.distributor.distributor

import com.example.distributor.config.DistributorConfig
import com.example.distributor.distributor.model.OrderResult
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import javax.annotation.PostConstruct

@Service
class DistributorNotifier {

    val log = LoggerFactory.getLogger(this.javaClass)

    val queue: Deque<OrderResult> = ConcurrentLinkedDeque()

    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

    @Autowired
    lateinit var rabbit: RabbitTemplate

    @Autowired
    lateinit var distributorConfig: DistributorConfig

    fun register(order: OrderResult) {
        queue.offer(order)
    }


    @PostConstruct
    fun init() {
        GlobalScope.launch {
            while (true) {
                val order: OrderResult? = queue.poll()
                if (order != null) {
                    log.info("Process order: {}", order)
                    rabbit.convertAndSend(
                        order.exchange,
                        order.rotutingKey + "." + order.orderId,
                        gson.toJson(order)
                    )
                    if (order.result == "Created") {
                        order.result = "Delivered"
                        register(order)
                    }
                } else {
                    log.info("Empty queue")
                }

                Thread.sleep(100)
            }
        }
    }

}