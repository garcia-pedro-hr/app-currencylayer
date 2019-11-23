package com.phgarcia.currencylayercc.apis.currencylayer

import com.phgarcia.currencylayercc.BuildConfig
import com.phgarcia.currencylayercc.apis.currencylayer.responses.ListCurrenciesResponse
import com.phgarcia.currencylayercc.apis.currencylayer.responses.LiveExchangeRatesResponse
import retrofit2.Call
import retrofit2.http.GET

interface Endpoint {

    /***
     * Fetch currencylayer RestAPI supported currencies
     */
    @GET("list?access_key=" + BuildConfig.CURRENCYLAYER_KEY)
    fun getCurrencies(): Call<ListCurrenciesResponse>

    /***
     * Fetch exchange rates from currencylayer RestAPI
     * Note that with a free account it's only possible to get exchange rates from or to USD
     */
    @GET("live?access_key=" + BuildConfig.CURRENCYLAYER_KEY)
    fun getExchangeRates(): Call<LiveExchangeRatesResponse>

}