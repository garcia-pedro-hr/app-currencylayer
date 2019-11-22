package com.phgarcia.currencylayercc.screens.main

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.phgarcia.currencylayercc.R
import com.phgarcia.currencylayercc.database.room.currencies.CurrencyEntity
import com.phgarcia.currencylayercc.databinding.MainFragmentBinding
import com.phgarcia.currencylayercc.screens.main.adapters.CurrenciesAdapter
import com.phgarcia.currencylayercc.utils.round
import java.lang.Math.round
import kotlin.math.roundToLong

class MainFragment : Fragment() {

    private val logTag: String = MainFragment::class.java.simpleName

    companion object {
        fun newInstance() = MainFragment().apply { retainInstance = true }
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    private lateinit var currenciesAdapter: CurrenciesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        binding.mainViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initValueInputField()
        initCurrencyDropdown()
        initObservables()
    }

    private fun initValueInputField() {
        binding.valueInput.addTextChangedListener { text ->
            viewModel.setValueInput(text.toString().toDouble().round(2))
        }
    }

    private fun initCurrencyDropdown() {
        currenciesAdapter = CurrenciesAdapter(context!!, R.layout.currency_dropdown_menu_item)
        binding.currencyDropdown.setAdapter(currenciesAdapter)
        binding.currencyDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->
                viewModel.setSelectedCurrency(currenciesAdapter.getItem(p2))
            }
    }

    private fun initObservables() {
        viewModel.currenciesLiveData.observe(this,
            Observer<List<CurrencyEntity>> { currencies ->
                if (currencies.isNullOrEmpty()) viewModel.requestCurrencies()
                else currenciesAdapter.setData(currencies)
            })
    }

}
