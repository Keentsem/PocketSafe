package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_splits")
data class BillSplit(
    @PrimaryKey
    val id: String,
    val occasion: String,
    val totalAmount: Double,
    val memberCount: Int,
    val createdDate: Long,
    val shareCode: String
)
