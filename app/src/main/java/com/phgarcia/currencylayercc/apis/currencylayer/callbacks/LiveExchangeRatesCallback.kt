package com.phgarcia.currencylayercc.apis.currencylayer.callbacks

import android.util.Log
import com.phgarcia.currencylayercc.apis.currencylayer.responses.LiveExchangeRatesResponse
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LiveExchangeRatesCallback(
    private val databaseInstance: CurrencylayerDatabase
): Callback<LiveExchangeRatesResponse> {

    private val logTag = ListCurrenciesCallback::class.java.simpleName

    override fun onFailure(call: Call<LiveExchangeRatesResponse>, t: Throwable) {
        Log.e(logTag, "Failed to retrieve exchange rates from currencylayer API: ${t.message}.")
    }

    override fun onResponse(
        call: Call<LiveExchangeRatesResponse>,
        response: Response<LiveExchangeRatesResponse>
    ) {
        response.body()?.let { it ->
            val usdRates: MutableMap<String, Double> = mutableMapOf()
            it.quotes.forEach { exchangeRate ->
                /*
                 * Since the source of all exchange rates is USD (due to API limitations when
                 * using a free account), we can add all rates to a map in order to calculate
                 * the exchange rates between them.
                 */
                val source = exchangeRate.key.slice(0..2)
                val target = exchangeRate.key.slice(3..5)
                usdRates[target] = exchangeRate.value
                GlobalScope.launch {
                    addExchangeRateToDB(source, target, exchangeRate.value)
                }
            }

            /*
             * In order to calculate the exchange rate from a non-USD currency to another
             * non-USD currency, we first calculate the value in USD from the source currency
             * and then convert from USD to the target currency. E.g.:
             *      EUR -> BRL becomes EUR -> USD -> BRL
             */
            val currencies = usdRates.keys
            for (i in 0 until currencies.size) {
                val source = currencies.elementAt(i)
                GlobalScope.launch {
                    for (j in i + 1 until currencies.size) {
                        val target = currencies.elementAt(j)
                        if (usdRates[source] != null && usdRates[target] != null) {
                            val exchangeRate = usdRates[target]?.times(1 / (usdRates[source]?: -1.0))
                            if (exchangeRate != null && exchangeRate > 0) {
                                addExchangeRateToDB(source, target, exchangeRate)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun addExchangeRateToDB(source: String, target: String, rate: Double)
            = withContext(Dispatchers.IO) {
        databaseInstance.exchangeRatesDAO
            .insert(ExchangeRateEntity("$source$target", source, target, rate))
        databaseInstance.exchangeRatesDAO
            .insert(ExchangeRateEntity("$target$source", target, source, 1 / rate))
    }

}