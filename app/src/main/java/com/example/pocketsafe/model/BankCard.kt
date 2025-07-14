package com.example.pocketsafe.model

/**
 * Model class representing a bank card
 */
data class BankCard(
    val id: String,
    val name: String,
    val type: String, // "visa", "mastercard", "other"
    val lastFourDigits: String,
    val limit: Double,
    val spent: Double,
    val color: Int, // Card color
    val isLocked: Boolean = false, // New field for card locking
    val profileImageUrl: String? = null // New field for profile picture
)
