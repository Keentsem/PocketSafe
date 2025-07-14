package com.example.pocketsafe.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for BankCard entities
 * Follows the pixel-retro theme pattern of other DAOs in the app
 */
@Dao
interface BankCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(bankCard: BankCard)
    
    @Update
    suspend fun updateCard(bankCard: BankCard)
    
    @Delete
    suspend fun deleteCard(bankCard: BankCard)
    
    @Query("SELECT * FROM bank_cards WHERE isActive = 1 ORDER BY cardName ASC")
    fun getAllActiveCards(): LiveData<List<BankCard>>
    
    @Query("SELECT * FROM bank_cards ORDER BY cardName ASC")
    fun getAllCards(): LiveData<List<BankCard>>
    
    @Query("SELECT * FROM bank_cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): BankCard?
    
    @Query("UPDATE bank_cards SET currentSpending = :amount WHERE id = :cardId")
    suspend fun updateCardSpending(cardId: String, amount: Double)
    
    @Query("UPDATE bank_cards SET monthlyLimit = :limit WHERE id = :cardId")
    suspend fun updateCardLimit(cardId: String, limit: Double)
    
    @Query("UPDATE bank_cards SET isActive = :isActive WHERE id = :cardId")
    suspend fun setCardActive(cardId: String, isActive: Boolean)
    
    @Query("SELECT SUM(currentSpending) FROM bank_cards")
    suspend fun getTotalSpending(): Double?
    
    @Query("SELECT COUNT(*) FROM bank_cards WHERE currentSpending > monthlyLimit AND monthlyLimit > 0")
    fun getOverLimitCardsCount(): LiveData<Int>
}
