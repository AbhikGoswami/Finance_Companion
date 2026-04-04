package com.abhik.financecompanion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double = 0.0,
    val type: String = "",
    val category: String = "",
    val timestamp: Long = 0L,
    val note: String = ""
)