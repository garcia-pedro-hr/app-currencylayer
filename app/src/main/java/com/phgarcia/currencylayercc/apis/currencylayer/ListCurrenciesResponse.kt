package com.phgarcia.currencylayercc.apis.currencylayer

import com.google.gson.annotations.SerializedName

data class ListCurrenciesResponse (
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("terms")
    val terms: String,

    @SerializedName("privacy")
    val privacy: String,

    @SerializedName("currencies")
    val currencies: Map<String, String>
)