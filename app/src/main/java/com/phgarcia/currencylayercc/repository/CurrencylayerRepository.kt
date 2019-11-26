package com.phgarcia.currencylayercc.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.phgarcia.currencylayercc.apis.currencylayer.CurrencylayerApi
import com.phgarcia.currencylayercc.apis.currencylayer.responses.asDatabaseModel
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CurrencylayerRepository(private val database: CurrencylayerDatabase) {

    private val logTag: String = CurrencylayerRepository::class.java.simpleName

    val currencies: LiveData<List<CurrencyEntity>> = database.currenciesDAO.getAll()
    val targetCurrencies = MutableLiveData<List<CurrencyEntity>>()

    /**
     * Refresh currencies stored in offline cache
     */
    suspend fun refreshCurrencies() = withContext(Dispatchers.IO) {
        try {
            val currencies = CurrencylayerApi.retrofitService.getCurrenciesAsync()
            Log.i(logTag, "Currencies fetched from Currencylayer API")
            database.currenciesDAO.insertAll(currencies.asDatabaseModel())
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch currencies from Currencylayer API: ${e.message}")
        }
    }

    /**
     * Refresh exchange rates stored in offline cache
     */
    suspend fun refreshExchangeRates() = withContext(Dispatchers.IO) {
        try {
            val exchangeRates = CurrencylayerApi.retrofitService.getExchangeRatesAsync()
            Log.i(logTag, "Exchange rates fetched from Currencylayer API")
            database.exchangeRatesDAO.insertAll(exchangeRates.asDatabaseModel())
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch exchange rates from Currencylayer API: ${e.message}")
        }
    }

    /**
     * Get target currencies for a given source
     */
    suspend fun refreshTargetCurrenciesForSource(source: CurrencyEntity) =
        withContext(Dispatchers.IO) {
            val currencies = database.exchangeRatesDAO.getTargetsForSource(source.acronym)
            targetCurrencies.postValue(currencies)
        }

    /**
     * Get an exchange rate given a source currency and a target currency
     */
    suspend fun getExchangeRate(source: CurrencyEntity, target: CurrencyEntity): Double  =
        withContext(Dispatchers.IO) {
            database.exchangeRatesDAO.get(source.acronym, target.acronym)
        }

}