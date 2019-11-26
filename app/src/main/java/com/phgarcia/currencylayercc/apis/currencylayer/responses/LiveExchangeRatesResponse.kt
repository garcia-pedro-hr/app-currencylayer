package com.phgarcia.currencylayercc.apis.currencylayer.responses

import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity
import com.squareup.moshi.Json

data class LiveExchangeRatesResponse (
    @Json(name = "success") val success: Boolean,
    @Json(name = "terms") val terms: String,
    @Json(name = "privacy") val privacy: String,
    @Json(name = "timestamp") val timestamp: Int,
    @Json(name = "source") val source: String,
    @Json(name = "quotes") val quotes: Map<String, Double>
)

fun LiveExchangeRatesResponse.asDatabaseModel(): List<ExchangeRateEntity> {
    val exchangeRates: MutableList<ExchangeRateEntity> = mutableListOf()
    val usdRates: MutableMap<String, Double> = mutableMapOf()

    quotes.forEach {
        val source = it.key.slice(0..2)
        val target = it.key.slice(3..5)
        exchangeRates.add(ExchangeRateEntity("$source$target", source, target, it.value))
        exchangeRates.add(ExchangeRateEntity("$target$source", target, source, 1 / it.value))

        /*
         * Since the source of all exchange rates is USD (due to API limitations when
         * using a free account), we can add all rates to a map in order to calculate
         * the exchange rates between them.
         */
        usdRates[target] = it.value
    }

    /*
     * In order to calculate the exchange rate from a non-USD currency to another
     * non-USD currency, we first calculate the value in USD from the source currency
     * and then convert from USD to the target currency. E.g.:
     *      EUR -> BRL becomes EUR -> USD -> BRL
     */
    val currencies = usdRates.keys
    for (i in 0 until currencies.size) {
        for (j in i + 1 until currencies.size) {
            val source = currencies.elementAt(i)
            val target = currencies.elementAt(j)
            if (usdRates[source] != null && usdRates[target] != null) {
                val rate = usdRates[target]?.times(1 / (usdRates[source]?: -1.0))
                if (rate != null && rate > 0) {
                    exchangeRates.add(ExchangeRateEntity("$source$target", source, target, rate))
                    exchangeRates.add(ExchangeRateEntity("$target$source", target, source, 1 / rate))
                }
            }
        }
    }

    return exchangeRates
}