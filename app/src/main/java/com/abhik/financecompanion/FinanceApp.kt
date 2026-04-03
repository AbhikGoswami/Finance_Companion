package com.abhik.financecompanion

import android.app.Application
import com.abhik.financecompanion.data.AppDB
import com.abhik.financecompanion.data.TransactionRepo

class FinanceApplication : Application() {

    val database by lazy { AppDB.getDB(this) }
    val repository by lazy { TransactionRepo(database.transOp()) }
}