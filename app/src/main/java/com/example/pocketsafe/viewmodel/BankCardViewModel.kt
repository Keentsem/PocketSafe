package com.example.pocketsafe.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pocketsafe.MainApplication
import com.example.pocketsafe.data.BankCard
import com.example.pocketsafe.data.BankCardDao
import com.example.pocketsafe.data.CardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ViewModel for managing bank cards with spending limit functionality
 * Follows the pixel-retro theme of the PocketSafe app with gold (#f3c34e) and brown (#5b3f2c) colors
 */
class BankCardViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "BankCardViewModel"
    private val bankCardDao: BankCardDao
    
    // LiveData for all bank cards
    val allCards: LiveData<List<BankCard>>
    
    // LiveData for active cards only
    val activeCards: LiveData<List<BankCard>>
    
    // LiveData for cards over their spending limit
    val overLimitCardsCount: LiveData<Int>
    
    // Status message for user feedback
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    // Selected card for detail view
    private val _selectedCard = MutableLiveData<BankCard?>()
    val selectedCard: LiveData<BankCard?> = _selectedCard
    
    init {
        try {
            val db = MainApplication.getDatabase(application)
            bankCardDao = db.bankCardDao()
            allCards = bankCardDao.getAllCards()
            activeCards = bankCardDao.getAllActiveCards()
            overLimitCardsCount = bankCardDao.getOverLimitCardsCount()
            Log.d(TAG, "BankCardViewModel initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BankCardViewModel: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Add a new bank card
     */
    fun addCard(
        cardName: String,
        cardNumber: String,
        cardType: CardType,
        monthlyLimit: Double,
        color: String = "#f3c34e" // Default gold color from pixel-retro theme
    ) {
        try {
            if (cardName.isBlank()) {
                _statusMessage.value = "Card name cannot be empty"
                return
            }
            
            if (cardNumber.isBlank()) {
                _statusMessage.value = "Card number cannot be empty"
                return
            }
            
            // Only store last 4 digits for security
            val lastFourDigits = if (cardNumber.length >= 4) {
                cardNumber.takeLast(4)
            } else {
                cardNumber
            }
            
            val newCard = BankCard(
                id = UUID.randomUUID().toString(),
                cardName = cardName,
                cardNumber = lastFourDigits,
                cardType = cardType,
                monthlyLimit = monthlyLimit,
                color = color
            )
            
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bankCardDao.insertCard(newCard)
                }
                _statusMessage.value = "Card added successfully"
                Log.d(TAG, "Card added: $cardName")
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error adding card: ${e.message}"
            Log.e(TAG, "Error adding card: ${e.message}", e)
        }
    }
    
    /**
     * Update an existing card
     */
    fun updateCard(card: BankCard) {
        try {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bankCardDao.updateCard(card)
                }
                _statusMessage.value = "Card updated successfully"
                Log.d(TAG, "Card updated: ${card.cardName}")
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error updating card: ${e.message}"
            Log.e(TAG, "Error updating card: ${e.message}", e)
        }
    }
    
    /**
     * Delete a card
     */
    fun deleteCard(card: BankCard) {
        try {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bankCardDao.deleteCard(card)
                }
                _statusMessage.value = "Card deleted successfully"
                Log.d(TAG, "Card deleted: ${card.cardName}")
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error deleting card: ${e.message}"
            Log.e(TAG, "Error deleting card: ${e.message}", e)
        }
    }
    
    /**
     * Update card spending amount
     */
    fun updateCardSpending(cardId: String, amount: Double) {
        try {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bankCardDao.updateCardSpending(cardId, amount)
                }
                _statusMessage.value = "Spending updated"
                Log.d(TAG, "Card spending updated: $cardId, amount: $amount")
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error updating spending: ${e.message}"
            Log.e(TAG, "Error updating card spending: ${e.message}", e)
        }
    }
    
    /**
     * Update card monthly limit
     */
    fun updateCardLimit(cardId: String, limit: Double) {
        try {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bankCardDao.updateCardLimit(cardId, limit)
                }
                _statusMessage.value = "Limit updated"
                Log.d(TAG, "Card limit updated: $cardId, limit: $limit")
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error updating limit: ${e.message}"
            Log.e(TAG, "Error updating card limit: ${e.message}", e)
        }
    }
    
    /**
     * Toggle card active status
     */
    fun toggleCardActive(cardId: String, isActive: Boolean) {
        try {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    bankCardDao.setCardActive(cardId, isActive)
                }
                val status = if (isActive) "activated" else "deactivated"
                _statusMessage.value = "Card $status"
                Log.d(TAG, "Card active status updated: $cardId, isActive: $isActive")
            }
        } catch (e: Exception) {
            _statusMessage.value = "Error updating card status: ${e.message}"
            Log.e(TAG, "Error updating card active status: ${e.message}", e)
        }
    }
    
    /**
     * Get total spending across all cards
     */
    suspend fun getTotalSpending(): Double {
        return try {
            withContext(Dispatchers.IO) {
                bankCardDao.getTotalSpending() ?: 0.0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting total spending: ${e.message}", e)
            0.0
        }
    }
    
    /**
     * Set the selected card for detail view
     */
    fun selectCard(card: BankCard) {
        _selectedCard.value = card
    }
    
    /**
     * Clear the selected card
     */
    fun clearSelectedCard() {
        _selectedCard.value = null
    }
    
    /**
     * Reset status message after it's been displayed
     */
    fun resetStatusMessage() {
        _statusMessage.value = ""
    }
}
