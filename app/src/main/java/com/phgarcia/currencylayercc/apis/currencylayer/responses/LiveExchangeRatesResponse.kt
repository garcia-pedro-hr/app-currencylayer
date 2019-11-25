package com.phgarcia.currencylayercc.apis.currencylayer.responses

import com.squareup.moshi.Json

data class LiveExchangeRatesResponse (
    @Json(name = "success") val success: Boolean,
    @Json(name = "terms") val terms: String,
    @Json(name = "privacy") val privacy: String,
    @Json(name = "timestamp") val timestamp: Int,
    @Json(name = "source") val source: String,
    @Json(name = "quotes") val quotes: Map<String, Double>
)