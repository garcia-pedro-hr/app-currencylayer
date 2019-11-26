package com.phgarcia.currencylayercc.screens.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.phgarcia.currencylayercc.database.room.CurrencylayerDatabase
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.phgarcia.currencylayercc.repository.CurrencylayerRepository
import com.phgarcia.currencylayercc.utils.round
import kotlinx.coroutines.*
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val logTag: String = MainViewModel::class.java.simpleName

    private val currencylayerRepository =
        CurrencylayerRepository(CurrencylayerDatabase.getInstance(application))

    val sourceCurrenciesLiveData: LiveData<List<CurrencyEntity>> =
        currencylayerRepository.currencies
    val targetCurrenciesLiveData: LiveData<List<CurrencyEntity>> =
        currencylayerRepository.targetCurrencies

    private val conversionResultLiveData = MediatorLiveData<Double>()
    private val valueInputLiveData = MutableLiveData<Double>()
    private val sourceCurrencyLiveData = MutableLiveData<CurrencyEntity>()
    private val targetCurrencyLiveData = MutableLiveData<CurrencyEntity>()

    init {
        refreshDataFromRepository()

        conversionResultLiveData.addSource(valueInputLiveData) { updateConversionResult() }
        conversionResultLiveData.addSource(sourceCurrencyLiveData) { updateConversionResult() }
        conversionResultLiveData.addSource(targetCurrencyLiveData) { updateConversionResult() }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

    fun getSourceCurrencyObservable(): LiveData<CurrencyEntity> = sourceCurrencyLiveData
    fun getConversionResultObservable(): LiveData<Double> = conversionResultLiveData

    fun setValueInput(value: Double?) = valueInputLiveData.postValue(value)
    fun setSourceCurrency(currency: CurrencyEntity?) = sourceCurrencyLiveData.postValue(currency)
    fun setTargetCurrency(currency: CurrencyEntity?) = targetCurrencyLiveData.postValue(currency)

    fun updateTargetCurrenciesList(source: CurrencyEntity?) = viewModelScope.launch {
        source?.let {
            currencylayerRepository.refreshTargetCurrenciesForSource(it)
        }
    }

    private fun refreshDataFromRepository() = viewModelScope.launch {
        try {
            currencylayerRepository.refreshCurrencies()
            currencylayerRepository.refreshExchangeRates()
        } catch (e: IOException) {
            Log.e(logTag, "Failed to refresh data from repository: $e")
        }
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
                        currencylayerRepository.getExchangeRate(source, target)
                    Log.d(logTag, "Conversion rate is $rate")
                    conversionResultLiveData.postValue((input * rate).round(4))
                }
            }
        } else conversionResultLiveData.postValue(null)
    }

}
