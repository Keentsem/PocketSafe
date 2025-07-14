package com.example.pocketsafe.data.repository

import com.example.pocketsafe.data.Subscription
import com.example.pocketsafe.data.dao.SubscriptionDao
import com.example.pocketsafe.firebase.FirebaseService
import kotlinx.coroutines.flow.Flow

/**
 * Repository that handles subscription data operations with both local database and Firebase
 * Modified to work without Hilt to prevent app crashes
 */
class SubscriptionRepository(
    private val subscriptionDao: SubscriptionDao
) {
    // Get Firebase service using singleton pattern
    private val firebaseService: FirebaseService = FirebaseService.getInstance()
    // Local database operations
    fun getAllSubscriptions(): Flow<List<Subscription>> = 
        subscriptionDao.getAllSubscriptions()
        
    fun getActiveSubscriptions(): Flow<List<Subscription>> =
        subscriptionDao.getActiveSubscriptions()
        
    suspend fun getSubscriptionById(id: Long): Subscription? =
        subscriptionDao.getSubscriptionById(id)
        
    // Save to both local and Firebase
    suspend fun saveSubscription(subscription: Subscription) {
        if (subscription.id == 0L) {
            // Insert into Room first to get the generated ID
            val localId = subscriptionDao.insertSubscription(subscription)
            val subscriptionWithId = subscription.copy(id = localId)
            // Now save to Firebase with the correct ID
            firebaseService.saveSubscription(subscriptionWithId)
        } else {
            // Update both Room and Firebase
            subscriptionDao.updateSubscription(subscription)
            firebaseService.updateSubscription(subscription)
        }
    }
    
    suspend fun updateSubscription(subscription: Subscription) {
        // Update in Firebase
        firebaseService.updateSubscription(subscription)
        
        // Update in local database
        subscriptionDao.updateSubscription(subscription)
    }
    
    suspend fun deleteSubscription(subscription: Subscription) {
        // Delete from Firebase
        firebaseService.deleteSubscription(subscription.id.toString())
        
        // Delete from local database
        subscriptionDao.deleteSubscription(subscription)
    }
    
    // Sync data from Firebase to local database
    suspend fun syncSubscriptions() {
        try {
            val subscriptions = firebaseService.fetchAllSubscriptions()
            for (subscription in subscriptions) {
                subscriptionDao.insertSubscription(subscription)
            }
        } catch (e: Exception) {
            // Handle errors - maybe log or show a message
        }
    }
}
