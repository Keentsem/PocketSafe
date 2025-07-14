package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Data class representing a bank card with spending limit functionality
 * Follows the pixel-retro theme of PocketSafe app
 */
@Entity(tableName = "bank_cards")
data class BankCard(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val cardNumber: String = "",        // Last 4 digits only for security
    val cardName: String = "",          // Name on card or nickname
    val cardType: CardType = CardType.VISA,
    val monthlyLimit: Double = 0.0,     // Monthly spending limit
    val currentSpending: Double = 0.0,  // Current month's spending
    val isActive: Boolean = true,       // Whether card is active or disabled
    val color: String = "#f3c34e"       // Card color in hex (default: gold)
)

/**
 * Enum representing different card types with their corresponding sprites
 */
enum class CardType(val displayName: String, val drawableRes: String) {
    VISA("Visa", "visa"),
    MASTERCARD("Mastercard", "mastercard"),
    OTHER("Other Card", "banking")
}
