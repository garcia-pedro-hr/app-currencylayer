package com.phgarcia.currencylayercc.screens.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.phgarcia.currencylayercc.R
import com.phgarcia.currencylayercc.database.room.entities.CurrencyEntity
import com.phgarcia.currencylayercc.databinding.MainFragmentBinding
import com.phgarcia.currencylayercc.screens.main.adapters.CurrenciesAdapter
import com.phgarcia.currencylayercc.utils.hideKeyboard
import com.phgarcia.currencylayercc.utils.round

class MainFragment : Fragment() {

    private val logTag: String = MainFragment::class.java.simpleName

    companion object {
        fun newInstance() = MainFragment().apply { retainInstance = true }
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    private lateinit var sourceCurrenciesAdapter: CurrenciesAdapter
    private lateinit var targetCurrenciesAdapter: CurrenciesAdapter

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
        initExchangeDropdown()
        initObservables()
    }

    private fun initValueInputField() {
        binding.valueInput.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                viewModel.setValueInput(text.toString().toDouble().round(4))
            } else viewModel.setValueInput(null)
        }
    }

    private fun initCurrencyDropdown() {
        sourceCurrenciesAdapter = CurrenciesAdapter(context!!, R.layout.currency_dropdown_menu_item)
        binding.currencyDropdown.setAdapter(sourceCurrenciesAdapter)
        binding.currencyDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->
                viewModel.setSourceCurrency(sourceCurrenciesAdapter.getItem(p2))
            }
        binding.currencyDropdown.onFocusChangeListener =
            View.OnFocusChangeListener { view, isFocused ->
                if (view == binding.currencyDropdown && isFocused) hideKeyboard()
            }
    }

    private fun initExchangeDropdown() {
        targetCurrenciesAdapter = CurrenciesAdapter(context!!, R.layout.currency_dropdown_menu_item)
        binding.exchangeDropdown.setAdapter(targetCurrenciesAdapter)
        binding.exchangeDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->
                viewModel.setTargetCurrency(targetCurrenciesAdapter.getItem(p2))
            }
        binding.exchangeDropdown.onFocusChangeListener =
            View.OnFocusChangeListener { view, isFocused ->
                if (view == binding.exchangeDropdown && isFocused) hideKeyboard()
            }
    }

    private fun initObservables() {
        viewModel.sourceCurrenciesLiveData.observe(this,
            Observer<List<CurrencyEntity>> { currencies ->
                if (!currencies.isNullOrEmpty()) {
                    sourceCurrenciesAdapter.setData(currencies)
                }
            })

        viewModel.targetCurrenciesLiveData.observe(this,
            Observer<List<CurrencyEntity>> { currencies ->
                targetCurrenciesAdapter.setData(currencies)
            })

        viewModel.getSourceCurrencyObservable().observe(this,
            Observer { currency -> viewModel.updateTargetCurrenciesList(currency) })
    }

}
