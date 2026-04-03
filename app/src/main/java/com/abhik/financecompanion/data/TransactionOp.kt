package com.abhik.financecompanion.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import  androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionOp {

    @Insert
    suspend fun insertTrans(transaction: Transaction)

    @Delete
    suspend fun deleteTrans(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :transactionType")
    fun getTotalAmountByType(transactionType: String): Flow<Double?>
}