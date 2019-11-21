package com.phgarcia.currencylayercc.apis.currencylayer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class CurrencylayerAPI {

    companion object {
        private const val BASE_URL = "http://apilayer.net/api/"
        fun getInstance(): Retrofit =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

}