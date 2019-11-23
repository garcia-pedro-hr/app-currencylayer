package com.phgarcia.currencylayercc.apis.currencylayer.callbacks

import android.util.Log
import com.phgarcia.currencylayercc.apis.currencylayer.responses.LiveExchangeRatesResponse
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            it.quotes.forEach { exchangeRate ->
                GlobalScope.launch { addExchangeRateToDB(exchangeRate) }
            }
        }
    }

    private suspend fun addExchangeRateToDB(exchangeRate: Map.Entry<String, Double>)
            = withContext(Dispatchers.IO) {
        val source = exchangeRate.key.slice(0..2)
        val target = exchangeRate.key.slice(3..5)
        GlobalScope.run {
            databaseInstance.exchangeRatesDAO
                .insert(ExchangeRateEntity("$source$target", source, target, exchangeRate.value))
            databaseInstance.exchangeRatesDAO
                .insert(ExchangeRateEntity("$target$source", target, source, 1 / exchangeRate.value))
        }
    }

}