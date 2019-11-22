package com.phgarcia.currencylayercc.screens.main

import android.app.Application
import android.content.ContentValues.TAG
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.phgarcia.currencylayercc.apis.currencylayer.CurrencylayerAPI
import com.phgarcia.currencylayercc.apis.currencylayer.Endpoint
import com.phgarcia.currencylayercc.apis.currencylayer.ListCurrenciesResponse
import com.phgarcia.currencylayercc.database.room.currencies.CurrenciesDatabase
import com.phgarcia.currencylayercc.database.room.currencies.CurrencyEntity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val logTag: String = MainViewModel::class.java.simpleName

    private val currenciesDatabase: CurrenciesDatabase = CurrenciesDatabase.getInstance(application)

    private val valueInputLiveData = MutableLiveData<Double>()
    private val selectedCurrencyLiveData = MutableLiveData<CurrencyEntity>()

    val currenciesLiveData: LiveData<List<CurrencyEntity>> =
        currenciesDatabase.dao.getAll()

    fun getValueInputObservable(): LiveData<Double> = valueInputLiveData
    fun getSelectedCurrencyObservable(): LiveData<CurrencyEntity> = selectedCurrencyLiveData

    fun setValueInput(value: Double) { valueInputLiveData.value = value }
    fun setSelectedCurrency(currency: CurrencyEntity?) { selectedCurrencyLiveData.value = currency }

    fun requestCurrencies() {
        val currencylayerClient = CurrencylayerAPI.getInstance()
        val endpoint = currencylayerClient.create(Endpoint::class.java)
        val callback = endpoint.getCurrencies()

        callback.enqueue(object: Callback<ListCurrenciesResponse> {
            override fun onResponse(
                call: Call<ListCurrenciesResponse>,
                response: Response<ListCurrenciesResponse>
            ) {
                response.body()?.let { it ->
                    it.currencies.forEach { currency ->
                        val entity = CurrencyEntity(currency.key, currency.value)
                        AsyncTask.execute { currenciesDatabase.dao.insert(entity) }
                    }
                }
            }

            override fun onFailure(call: Call<ListCurrenciesResponse>, t: Throwable) {
                Log.e(logTag, "Failed to retrieve currencies from currencylayer API: ${t.message}.")
            }
        })
    }

}
