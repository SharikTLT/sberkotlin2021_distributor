package com.example.distributor.distributor.model

import com.google.gson.annotations.Expose

data class OrderResult(
    @Expose
    val orderId: String,

    val orderRaw: Order?,

    @Expose
    var result: String,

    @Expose
    val signature: String,

    val exchange: String,

    val rotutingKey: String

)