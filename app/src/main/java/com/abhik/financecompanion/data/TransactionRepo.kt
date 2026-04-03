package com.abhik.financecompanion.data

import kotlinx.coroutines.flow.Flow

class TransactionRepo (private val transop:TransactionOp){

    val alltrans: Flow<List<Transaction>> = transop.getAllTransactions()

    fun getTotalAmtByType(type:String): Flow<Double?>{
        return transop.getTotalAmountByType(type)
    }

    suspend fun insertTrans(transaction: Transaction){
        transop.insertTrans(transaction)
    }

    suspend fun deleteTrans(transaction: Transaction){
        transop.deleteTrans(transaction)
    }
}