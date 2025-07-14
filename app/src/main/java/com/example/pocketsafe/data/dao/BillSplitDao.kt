package com.example.pocketsafe.data.dao

import androidx.room.*
import com.example.pocketsafe.data.BillSplit
import com.example.pocketsafe.data.BillSplitMember

@Dao
interface BillSplitDao {
    @Insert
    suspend fun insertBillSplit(split: BillSplit)
    
    @Insert
    suspend fun insertMember(member: BillSplitMember)
    
    @Query("SELECT * FROM bill_splits WHERE shareCode = :code")
    suspend fun getBillSplitByCode(code: String): BillSplit?
    
    @Query("SELECT * FROM bill_split_members WHERE splitId = :splitId")
    suspend fun getMembersForSplit(splitId: String): List<BillSplitMember>
    
    @Query("SELECT COUNT(*) FROM bill_splits")
    suspend fun getTotalSplitsCount(): Int
    
    @Query("SELECT * FROM bill_splits ORDER BY createdDate DESC")
    suspend fun getAllBillSplits(): List<BillSplit>
    
    @Delete
    suspend fun deleteBillSplit(split: BillSplit)
    
    @Query("DELETE FROM bill_split_members WHERE splitId = :splitId")
    suspend fun deleteMembersForSplit(splitId: String)
}
