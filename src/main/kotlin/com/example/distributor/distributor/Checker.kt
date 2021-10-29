package com.example.distributor.distributor

import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils

@Service
class Checker {
    val salt = "check_student_result"

    fun hash(source: String): String {
        return DigestUtils.md5DigestAsHex((salt+source).toByteArray())
    }
}