package com.phgarcia.currencylayercc.database.room.currencies

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CurrenciesDatabaseDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currencyEntity: CurrencyEntity)

    @Query("SELECT * FROM currencies_table ORDER BY currency_acronym")
    fun getAll(): LiveData<List<CurrencyEntity>>

    @Query("DELETE FROM currencies_table")
    fun clear()

}