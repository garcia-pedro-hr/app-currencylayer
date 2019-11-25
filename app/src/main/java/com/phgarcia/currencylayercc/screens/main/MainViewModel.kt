package com.phgarcia.currencylayercc.screens.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.phgarcia.currencylayercc.apis.currencylayer.CurrencylayerApi
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity
import com.phgarcia.currencylayercc.utils.round
import kotlinx.coroutines.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val logTag: String = MainViewModel::class.java.simpleName

    private val currencylayerDatabase: CurrencylayerDatabase = CurrencylayerDatabase.getInstance(application)

    private val conversionResultLiveData = MediatorLiveData<Double>()
    private val valueInputLiveData = MutableLiveData<Double>()
    private val sourceCurrencyLiveData = MutableLiveData<CurrencyEntity>()
    private val targetCurrencyLiveData = MutableLiveData<CurrencyEntity>()
    private val targetCurrenciesLiveData = MutableLiveData<List<CurrencyEntity>>()

    val sourceCurrenciesLiveData: LiveData<List<CurrencyEntity>> =
        currencylayerDatabase.currenciesDAO.getAll()

    init {
        conversionResultLiveData.addSource(valueInputLiveData) { updateConversionResult() }
        conversionResultLiveData.addSource(sourceCurrencyLiveData) { updateConversionResult() }
        conversionResultLiveData.addSource(targetCurrencyLiveData) { updateConversionResult() }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun getSourceCurrencyObservable(): LiveData<CurrencyEntity> = sourceCurrencyLiveData
    fun getTargetCurrenciesObservable(): LiveData<List<CurrencyEntity>> = targetCurrenciesLiveData
    fun getConversionResultObservable(): LiveData<Double> = conversionResultLiveData

    fun setValueInput(value: Double?) = valueInputLiveData.postValue(value)
    fun setSourceCurrency(currency: CurrencyEntity?) = sourceCurrencyLiveData.postValue(currency)
    fun setTargetCurrency(currency: CurrencyEntity?) = targetCurrencyLiveData.postValue(currency)

    fun clearTargetCurrency() { targetCurrencyLiveData.value = null }

    fun updateTargetCurrenciesList(source: CurrencyEntity?) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            if (source != null) {
                val targetCurrencies: MutableList<CurrencyEntity> = mutableListOf()
                targetCurrencies.addAll(currencylayerDatabase.exchangeRatesDAO
                    .getTargetsForSource(source.acronym))
                targetCurrenciesLiveData.postValue(targetCurrencies)
            }
        }
    }

    fun updateCurrenciesFromApi() = GlobalScope.launch {
        requestCurrencies()
        requestExchangeRates()
    }

    private fun updateConversionResult() {
        val source: CurrencyEntity? = sourceCurrencyLiveData.value
        val target: CurrencyEntity? = targetCurrencyLiveData.value
        val input: Double? = valueInputLiveData.value

        if (source != null && target != null && input != null) {
            Log.d(logTag, "Converting from $source to  $target")
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    val rate: Double =
                        currencylayerDatabase.exchangeRatesDAO.get(source.acronym, target.acronym)
                    Log.d(logTag, "Conversion rate is $rate")
                    conversionResultLiveData.postValue((input * rate).round(4))
                }
            }
        } else conversionResultLiveData.postValue(null)
    }

    private suspend fun requestCurrencies() = withContext(Dispatchers.IO) {
        try {
            val result = CurrencylayerApi.retrofitService.getCurrenciesAsync()
            Log.i(logTag, "Currencies fetched from Currencylayer API")

            result.currencies.forEach { currency ->
                addCurrencyToDatabase(currency.key, currency.value)
            }
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch currencies from Currencylayer API: ${e.message}")
        }
    }

    private suspend fun requestExchangeRates() = withContext(Dispatchers.IO) {
        try {
            val result = CurrencylayerApi.retrofitService.getExchangeRatesAsync()
            Log.i(logTag, "Exchange rates fetched from Currencylayer API")

            val usdRates: MutableMap<String, Double> = mutableMapOf()
            result.quotes.forEach { exchangeRate ->
                /*
                 * Since the source of all exchange rates is USD (due to API limitations when
                 * using a free account), we can add all rates to a map in order to calculate
                 * the exchange rates between them.
                 */
                val source = exchangeRate.key.slice(0..2)
                val target = exchangeRate.key.slice(3..5)
                usdRates[target] = exchangeRate.value
                addExchangeRateToDatabase(source, target, exchangeRate.value)
                addExchangeRateToDatabase(target, source, 1 / exchangeRate.value)
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
                        val exchangeRate = usdRates[target]?.times(1 / (usdRates[source]?: -1.0))
                        if (exchangeRate != null && exchangeRate > 0) {
                            addExchangeRateToDatabase(source, target, exchangeRate)
                            addExchangeRateToDatabase(target, source, 1 / exchangeRate)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Failed to fetch exchange rates from Currencylayer API: ${e.message}")
        }
    }

    private suspend fun addCurrencyToDatabase(acronym: String, name: String) =
        withContext(Dispatchers.IO) {
            val entity = CurrencyEntity(acronym, name)
            currencylayerDatabase.currenciesDAO.insert(entity)
        }

    private suspend fun addExchangeRateToDatabase(source: String, target: String, rate: Double) =
        withContext(Dispatchers.IO) {
            val entity = ExchangeRateEntity("$source$rate", source, target, rate)
            currencylayerDatabase.exchangeRatesDAO.insert(entity)
        }

}
