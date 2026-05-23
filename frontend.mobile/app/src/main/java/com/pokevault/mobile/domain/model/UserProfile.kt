package com.pokevault.mobile.domain.model

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val balance: Double,
    val isVip: Boolean,
)
