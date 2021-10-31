package com.example.distributor.debug

import com.example.distributor.config.DistributorConfig
import com.example.distributor.distributor.model.Item
import com.example.distributor.distributor.model.Order
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.converter.SimpleMessageConverter
import org.springframework.stereotype.Service
import java.time.Instant
import javax.annotation.PostConstruct

@Service
class SampleProducer {

    val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    lateinit var rabbit : RabbitTemplate

    @Value("\${sample.producer.enabled:false}")
    var enabled: Boolean = false

    @Value("\${sample.producer.backRoutingKey:sampleProducer.orderNotify}")
    var backRoutingKey: String = ""

    @Autowired
    lateinit var distributorConfig: DistributorConfig

    @PostConstruct
    fun init(){
        if(!enabled){
            log.info("SampleProducer is DISABLED")
            return
        }
        log.info("Start SampleProducer")
        val key = distributorConfig.inputKey.replace("#", "sampleProducer")
        val conv = SimpleMessageConverter()
        GlobalScope.launch {
            val gson = Gson()
            var counter = 3L
            while (--counter > 0 ){
                //val props: MutableMap<String, Any> = HashMap()

                val payload = gson.toJson(newOrder())
                val props = MessageProperties()
                props.contentType = "text/plain"
                props.contentEncoding = "UTF-8"
                props.setHeader("Notify-Exchange", distributorConfig.exchange)
                props.setHeader("Notify-RoutingKey", backRoutingKey)


                val message = Message(payload.toByteArray(), props)


                rabbit.send(distributorConfig.exchange, key, message)

                log.info("Sended {}", message)
                Thread.sleep(100)
            }
        }
        log.info("Go next")

    }

    private fun newOrder(): Order {
        return Order(
            Instant.now().toEpochMilli().toString(),
            "address",
            "recipient",
            listOf(Item(1, "Name"))
        )
    }

}