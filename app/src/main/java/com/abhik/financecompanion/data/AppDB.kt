package com.abhik.financecompanion.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDB:RoomDatabase() {

    abstract fun transOp(): TransactionOp

    companion object{
        @Volatile
        private var Instance: AppDB?=null

        fun getDB(context: Context):AppDB{
            return Instance?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "financial_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance=it }
            }
        }
    }
}