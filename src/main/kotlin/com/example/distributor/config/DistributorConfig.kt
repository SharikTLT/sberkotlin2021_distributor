package com.example.distributor.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class DistributorConfig {

    @Value("\${distributor.exchange}")
    var exchange: String = "distributor"

    @Value("\${distributor.order_queue}")
    var queue: String = "distributor"

    @Value("\${distributor.input_key}")
    var inputKey: String = "distributor"

}