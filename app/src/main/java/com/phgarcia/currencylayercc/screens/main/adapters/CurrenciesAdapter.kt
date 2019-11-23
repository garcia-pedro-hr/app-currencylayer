package com.phgarcia.currencylayercc.screens.main.adapters

import android.content.Context
import android.widget.ArrayAdapter
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import kotlinx.coroutines.GlobalScope

class CurrenciesAdapter(
    context: Context,
    layoutResId: Int
) : ArrayAdapter<CurrencyEntity>(context, layoutResId) {

    fun setData(data: List<CurrencyEntity>) {
        GlobalScope.run {
            clear()
            addAll(data)
            notifyDataSetChanged()
        }
    }

}