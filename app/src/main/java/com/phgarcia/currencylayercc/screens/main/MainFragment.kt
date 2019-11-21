package com.phgarcia.currencylayercc.screens.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.phgarcia.currencylayercc.R
import com.phgarcia.currencylayercc.database.room.currencies.CurrencyEntity

class MainFragment : Fragment() {

    private val logTag: String = MainFragment::class.java.simpleName

    companion object {
        fun newInstance() = MainFragment().apply { retainInstance = true }
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        initObservables()
    }

    private fun initObservables() {
        viewModel.currenciesLiveData.observe(this,
            Observer<List<CurrencyEntity>> { currencies ->
                if (currencies.isNullOrEmpty()) viewModel.requestCurrencies()
                else Log.d(logTag, "UPDATING UI")// TODO: Update currency list when UI is available
            })
    }

}
