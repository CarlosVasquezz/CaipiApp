package com.example.caipiapp.model

data class User(
    var email: String = "",
    var role: String = "",
    var children: Map<String, Child> = emptyMap()
)
