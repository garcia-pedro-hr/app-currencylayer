package com.phgarcia.currencylayercc.database.room.currencies

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies_table")
data class CurrencyEntity (
    @PrimaryKey
    @ColumnInfo(name = "currency_acronym")
    val acronym: String = "",

    @ColumnInfo(name = "currency_name")
    val name: String = ""
) {

    override fun toString(): String =  "$acronym - $name"

}