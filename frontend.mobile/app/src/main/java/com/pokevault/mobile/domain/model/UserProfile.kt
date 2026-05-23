package com.pokevault.mobile.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val balance: Double,
    val isVip: Boolean,
)
