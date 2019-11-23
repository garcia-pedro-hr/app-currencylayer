package com.phgarcia.currencylayercc.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.phgarcia.currencylayercc.database.room.dao.CurrenciesDatabaseDAO
import com.phgarcia.currencylayercc.database.room.dao.ExchangeRatesDatabaseDAO
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity

@Database(entities = [CurrencyEntity::class, ExchangeRateEntity::class], version = 1, exportSchema = false)
abstract class CurrencylayerDatabase : RoomDatabase() {

    abstract val currenciesDAO: CurrenciesDatabaseDAO
    abstract val exchangeRatesDAO: ExchangeRatesDatabaseDAO

    companion object {
        @Volatile
        private var INSTANCE: CurrencylayerDatabase? = null

        fun getInstance(context: Context): CurrencylayerDatabase {
            synchronized(this) {
                var instance =
                    INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CurrencylayerDatabase::class.java,
                        "currencylayer_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }

}