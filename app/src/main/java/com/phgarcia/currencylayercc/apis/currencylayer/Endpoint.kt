package com.phgarcia.currencylayercc.apis.currencylayer

import com.phgarcia.currencylayercc.BuildConfig
import retrofit2.Call
import retrofit2.http.GET

interface Endpoint {

    @GET("list?access_key=" + BuildConfig.CURRENCYLAYER_KEY)
    fun getCurrencies(): Call<ListCurrenciesResponse>

}