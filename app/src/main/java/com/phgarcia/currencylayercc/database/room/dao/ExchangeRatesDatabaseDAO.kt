package com.phgarcia.currencylayercc.database.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity

@Dao
interface ExchangeRatesDatabaseDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currencyEntity: ExchangeRateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(currencyEntities: List<ExchangeRateEntity>)

    @Query("""
        SELECT * FROM currencies_table INNER JOIN exchange_rates_table 
        ON currency_acronym = target_currency_id
        WHERE source_currency_id = :sourceCurrencyId
        ORDER BY target_currency_id
        """)
    fun getTargetsForSource(sourceCurrencyId: String): List<CurrencyEntity>

    @Query("""
        SELECT exchange_rate FROM exchange_rates_table 
        WHERE source_currency_id = :sourceCurrencyId
            AND target_currency_id = :targetCurrencyId
        """)
    fun get(sourceCurrencyId: String, targetCurrencyId: String): Double

    @Query("DELETE FROM currencies_table")
    fun clear()

}