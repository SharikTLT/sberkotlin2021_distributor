package com.example.distributor.distributor

import com.example.distributor.config.DistributorConfig
import com.example.distributor.config.ListenerAdapter
import com.example.distributor.distributor.model.Order
import com.example.distributor.distributor.model.OrderResult
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
        log.info("Received raw: {}", message)
        try{
            val msg_str = String(message.body)
            val order = gson.fromJson(msg_str, Order::class.java)
            log.info("Received: {}", order)
            notifier.register(buildResult(message, order.uid, "Created", order))
        }catch (e: Exception) {
            notifier.register(buildResult(message, "errors", e.message!!, null))
        }
    }

    private fun buildResult(
        message: Message,
        uid: String,
        result: String,
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
            return checker.hash(order.uid+key)
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

