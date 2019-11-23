package com.phgarcia.currencylayercc.apis.currencylayer.callbacks

import android.util.Log
import com.phgarcia.currencylayercc.apis.currencylayer.responses.ListCurrenciesResponse
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListCurrenciesCallback(
    private val databaseInstance: CurrencylayerDatabase
): Callback<ListCurrenciesResponse> {

    private val logTag = ListCurrenciesCallback::class.java.simpleName

    override fun onFailure(call: Call<ListCurrenciesResponse>, t: Throwable) {
        Log.e(logTag, "Failed to retrieve currencies from currencylayer API: ${t.message}.")
    }

    override fun onResponse(
        call: Call<ListCurrenciesResponse>,
        response: Response<ListCurrenciesResponse>
    ) {
        response.body()?.let { it ->
            it.currencies.forEach { currency ->
                GlobalScope.launch { addCurrencyToDB(currency) }
            }
        }
    }

    private suspend fun addCurrencyToDB(currency: Map.Entry<String, String>)
            = withContext(Dispatchers.IO) {
        val entity = CurrencyEntity(currency.key, currency.value)
        databaseInstance.currenciesDAO.insert(entity)
    }

}