package com.example.geozilla.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    var address: String = "",
    val linkedUsers: List<String> = listOf(),
    var latitude:Double=0.0,
    var longitude:Double=0.0
)

