package com.example.distributor.distributor

import com.example.distributor.config.DistributorConfig
import com.example.distributor.config.ListenerAdapter
import com.example.distributor.distributor.model.Order
import com.example.distributor.distributor.model.OrderResult
import com.example.distributor.distributor.model.OrderStatus
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import java.security.MessageDigest

@Service
class DistributorListenerAdapter: ListenerAdapter {

    val log = LoggerFactory.getLogger(this.javaClass)

    val gson = Gson()


    @Autowired
    lateinit var service: DistributionService

    @Autowired
    lateinit var notifier: DistributorNotifier

    @Autowired
    lateinit var distributorConfig: DistributorConfig

    @Autowired
    lateinit var checker: Checker

    override fun receive(message: Message) {
        log.info("Received raw message: {}", message)
        try{
            val msg_str = String(message.body)
            val order = gson.fromJson(msg_str, Order::class.java)
            log.info("Received order: {}", order)
            notifier.register(buildResult(message, order.id, OrderStatus.CREATED, order))
        }catch (e: Exception) {
            log.error(e.message, e)
            notifier.register(buildResult(message, "errors", OrderStatus.ERROR, null))
        }
    }

    private fun buildResult(
        message: Message,
        uid: String,
        result: OrderStatus,
        order: Order?
    ): OrderResult {
        val rotutingKey = extractHeader(message, "Notify-RoutingKey", "distribution.updates.unknown")
        val signature = sign(order, rotutingKey)
        return OrderResult(
            uid,
            order,
            result,
            signature,
            extractHeader(message, "Notify-Exchange", distributorConfig.exchange),
            rotutingKey
        )
    }


    private fun sign(order: Order?, key: String): String {
        if(order == null){
            return checker.hash("null"+key)
        }else{
            return checker.hash(order.id+key)
        }

    }



    private fun extractHeader(message: Message, key: String, defaultValue: String): String{
        var value = message.messageProperties.getHeader<String>(key)
        if (value == null) {
            value = defaultValue
        }
        return value
    }



}

