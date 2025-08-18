package com.example.risaleezanvakti.data.remote

data class ApiListResponse<T>(
    val status: String,
    val data: List<T>
)
