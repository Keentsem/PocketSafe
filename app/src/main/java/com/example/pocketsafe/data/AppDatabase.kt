package com.example.pocketsafe.data

import android.content.Context
import androidx.room.*
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.pocketsafe.data.dao.*
import java.util.*

// FIXED: Renamed to avoid conflict with existing Converters class
class DatabaseConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromIconType(iconType: IconType): String = iconType.name

    @TypeConverter
    fun toIconType(value: String): IconType = IconType.valueOf(value)
}

@Database(
    entities = [
        Expense::class,
        Category::class,
        BudgetGoal::class,
        Subscription::class,
        User::class,
        BillReminder::class,
        SavingsGoal::class,
        Account::class,
        BankCard::class,
        BillSplit::class,
        BillSplitMember::class
    ],
    version = 13,  // Increment version for new entities
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // Core DAOs that definitely exist
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetGoalDao(): BudgetGoalDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun userDao(): UserDao
    abstract fun billReminderDao(): BillReminderDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun accountDao(): AccountDao
    abstract fun bankCardDao(): BankCardDao
    abstract fun billSplitDao(): BillSplitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_safe_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
