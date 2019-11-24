package com.phgarcia.currencylayercc.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import kotlin.math.round

/***
 * Round a double to a certain number of decimals
 * Algorithm by fvasco from https://discuss.kotlinlang.org/t/how-do-you-round-a-number-to-n-decimal-places
 */
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun Fragment.hideKeyboard() {
    view?.let {
        val manager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(it.applicationWindowToken, 0)
    }
}