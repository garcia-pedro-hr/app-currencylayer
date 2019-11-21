package com.phgarcia.currencylayercc.database.room.currencies

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CurrencyEntity::class], version = 1, exportSchema = false)
abstract class CurrenciesDatabase : RoomDatabase() {

    abstract val dao: CurrenciesDatabaseDAO

    companion object {
        @Volatile
        private var INSTANCE: CurrenciesDatabase? = null

        fun getInstance(context: Context): CurrenciesDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CurrenciesDatabase::class.java,
                        "currencies_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }

}