package com.example.distributor.distributor.model

data class Order(
    val uid: String,
    val address: String,
    val recipient: String,
    val items: List<Item>
)