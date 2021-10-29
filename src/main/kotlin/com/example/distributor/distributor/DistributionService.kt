package com.example.distributor.distributor

import com.example.distributor.distributor.model.Item
import com.example.distributor.distributor.model.Order
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Service
class DistributionService {

   //val itemRemainings : Map<Long, Item> = ConcurrentHashMap()

    val orders : MutableMap<Long, Order> = ConcurrentHashMap()
    val index : AtomicLong = AtomicLong(0)

    fun processIncomeOrder(order: Order): Long{
        return placeOrder(order)
    }

    private fun placeOrder(order: Order): Long{
        val orderId = index.incrementAndGet()
        orders.put(orderId, order)
        return orderId
    }


}