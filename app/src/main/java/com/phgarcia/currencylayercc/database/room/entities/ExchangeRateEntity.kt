package com.phgarcia.currencylayercc.database.room.entities

import androidx.room.*

@Entity(tableName = "exchange_rates_table",
    foreignKeys = [
        ForeignKey(entity = CurrencyEntity::class,
            parentColumns = arrayOf("currency_acronym"),
            childColumns = arrayOf("source_currency_id"),
            onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CurrencyEntity::class,
            parentColumns = arrayOf("currency_acronym"),
            childColumns = arrayOf("target_currency_id"),
            onDelete = ForeignKey.CASCADE)])
data class ExchangeRateEntity (
    @PrimaryKey val id: String,

    @ColumnInfo(name = "source_currency_id", index = true)
    val sourceCurrencyId: String,

    @ColumnInfo(name = "target_currency_id", index = true)
    val targetCurrencyId: String,

    @ColumnInfo(name = "exchange_rate")
    val exchangeRate: Double
)