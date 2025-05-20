package com.example.taller3.model


data class User(
    val uid: String = "",
    val name: String = "",
    val idNumber: String = "",
    val email: String = "",
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isConnected: Boolean = false,
    val photoUrl: String = ""
)
