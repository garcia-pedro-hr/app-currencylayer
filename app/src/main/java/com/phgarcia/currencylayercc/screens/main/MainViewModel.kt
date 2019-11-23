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
import com.phgarcia.currencylayercc.database.room.entities.ExchangeRateEntity
import com.phgarcia.currencylayercc.utils.round
import kotlinx.coroutines.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val logTag: String = MainViewModel::class.java.simpleName

    private val currencylayerDatabase: CurrencylayerDatabase = CurrencylayerDatabase.getInstance(application)

    private val valueInputLiveData = MutableLiveData<Double>()
    private val conversionResultLiveData = MutableLiveData<Double>()
    private val selectedCurrencyLiveData = MutableLiveData<CurrencyEntity>()
    private val selectedTargetLiveData = MutableLiveData<CurrencyEntity>()
    private val targetCurrenciesLiveData = MutableLiveData<List<CurrencyEntity>>()

    val currenciesLiveData: LiveData<List<CurrencyEntity>> =
        currencylayerDatabase.currenciesDAO.getAll()

    fun getValueInputObservable(): LiveData<Double> = valueInputLiveData
    fun getSelectedCurrencyObservable(): LiveData<CurrencyEntity> = selectedCurrencyLiveData
    fun getTargetCurrenciesObservable(): LiveData<List<CurrencyEntity>> = targetCurrenciesLiveData
    fun getConversionResultObservable(): LiveData<Double> = conversionResultLiveData

    fun setValueInput(value: Double) { valueInputLiveData.value = value }
    fun setSelectedCurrency(currency: CurrencyEntity?) { selectedCurrencyLiveData.value = currency }
    fun setSelectedTarget(target: CurrencyEntity?) { selectedTargetLiveData.value = target }

    fun updateTargetCurrenciesList(source: CurrencyEntity?) {
        suspend fun doUpdate() = withContext(Dispatchers.IO) {
            if (source != null) {
                val targetCurrencies =
                    currencylayerDatabase.exchangeRatesDAO.getTargetsForSource(source.acronym)
                targetCurrenciesLiveData.postValue(targetCurrencies)

                println(targetCurrencies.joinToString(" "))
                println(targetCurrencies.contains(source))
            }
        }

        viewModelScope.launch {
            doUpdate()
        }
    }

    fun updateConversionResult(input: Double) {
        suspend fun doConversion(sourceId: String, targetId: String)
                = withContext(Dispatchers.IO) {
            val rate: Double = currencylayerDatabase.exchangeRatesDAO.get(sourceId, targetId)
            Log.d(logTag, "Conversion rate is $rate")
            conversionResultLiveData.postValue((input * rate).round(2))
        }

        val source: CurrencyEntity? = selectedCurrencyLiveData.value
        val target: CurrencyEntity? = selectedTargetLiveData.value

        if (source != null && target != null) {
            Log.d(logTag, "Converting from $source to  $target")
            viewModelScope.launch {
                doConversion(source.acronym, target.acronym)
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
