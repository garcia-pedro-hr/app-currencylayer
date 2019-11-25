package com.phgarcia.currencylayercc.apis.currencylayer

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

abstract class CurrencylayerApi {

    companion object {
        private const val BASE_URL = "http://apilayer.net/api/"

        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val retrofitService: CurrencylayerApiService by lazy {
            retrofit.create(CurrencylayerApiService::class.java)
        }
    }

}