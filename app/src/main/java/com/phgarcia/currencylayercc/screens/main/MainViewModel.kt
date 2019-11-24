package com.phgarcia.currencylayercc.screens.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.phgarcia.currencylayercc.apis.currencylayer.CurrencylayerAPI
import com.phgarcia.currencylayercc.apis.currencylayer.Endpoint
import com.phgarcia.currencylayercc.apis.currencylayer.callbacks.ListCurrenciesCallback
import com.phgarcia.currencylayercc.apis.currencylayer.callbacks.LiveExchangeRatesCallback
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.phgarcia.currencylayercc.utils.round
import kotlinx.coroutines.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val logTag: String = MainViewModel::class.java.simpleName

    private val currencylayerDatabase: CurrencylayerDatabase = CurrencylayerDatabase.getInstance(application)

    private val valueInputLiveData = MutableLiveData<Double>()
    private val conversionResultLiveData = MutableLiveData<Double>()
    private val sourceCurrencyLiveData = MutableLiveData<CurrencyEntity>()
    private val targetCurrencyLiveData = MutableLiveData<CurrencyEntity>()
    private val targetCurrenciesLiveData = MutableLiveData<List<CurrencyEntity>>()

    val sourceCurrenciesLiveData: LiveData<List<CurrencyEntity>> =
        currencylayerDatabase.currenciesDAO.getAll()

    fun getValueInputObservable(): LiveData<Double> = valueInputLiveData
    fun getSourceCurrencyObservable(): LiveData<CurrencyEntity> = sourceCurrencyLiveData
    fun getTargetCurrencyObservable(): LiveData<CurrencyEntity> = targetCurrencyLiveData
    fun getTargetCurrenciesObservable(): LiveData<List<CurrencyEntity>> = targetCurrenciesLiveData
    fun getConversionResultObservable(): LiveData<Double> = conversionResultLiveData

    fun setValueInput(value: Double) = valueInputLiveData.postValue(value)
    fun setSourceCurrency(currency: CurrencyEntity?) = sourceCurrencyLiveData.postValue(currency)
    fun setTargetCurrency(currency: CurrencyEntity?) = targetCurrencyLiveData.postValue(currency)

    fun updateTargetCurrenciesList(source: CurrencyEntity?) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (source != null) {
                    val targetCurrencies: MutableList<CurrencyEntity> = mutableListOf()
                    targetCurrencies.addAll(currencylayerDatabase.exchangeRatesDAO
                        .getTargetsForSource(source.acronym))
//                    sourceCurrencyLiveData.value?.let { targetCurrencies.add(it) }
                    targetCurrenciesLiveData.postValue(targetCurrencies)
                }
            }
        }
    }

    fun updateConversionResult() {
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
        }
    }

    fun requestCurrencies() =
        CurrencylayerAPI.getInstance()
            .create(Endpoint::class.java).getCurrencies()
            .enqueue(ListCurrenciesCallback(currencylayerDatabase))

    fun requestExchangeRates() =
        CurrencylayerAPI.getInstance()
            .create(Endpoint::class.java).getExchangeRates()
            .enqueue(LiveExchangeRatesCallback(currencylayerDatabase))

}
