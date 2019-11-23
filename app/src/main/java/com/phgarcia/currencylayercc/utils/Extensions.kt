package com.phgarcia.currencylayercc.utils

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