package com.phgarcia.currencylayercc.apis.currencylayer.responses

import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.squareup.moshi.Json

data class ListCurrenciesResponse (
    @Json(name = "success") val success: Boolean,
    @Json(name= "terms") val terms: String,
    @Json(name = "privacy") val privacy: String,
    @Json(name = "currencies") val currencies: Map<String, String>
)

fun ListCurrenciesResponse.asDatabaseModel(): List<CurrencyEntity> =
    currencies.map { CurrencyEntity(it.key, it.value) }
