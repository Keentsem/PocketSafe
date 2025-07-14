package com.example.pocketsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_split_members")
data class BillSplitMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val splitId: String,
    val name: String,
    val amount: Double
)
