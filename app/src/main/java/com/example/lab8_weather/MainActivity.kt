package com.example.lab8_weather

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lab8_weather.databinding.ActivityMainBinding
import com.example.lab8_weather.databinding.ItemForecastBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private var errorBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets(binding.root)
        binding.citySpinner.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, City.all.map { it.name })
        binding.citySpinner.setSelection(viewModel.selectedCity.coerceAtLeast(0))
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.loadCity(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.refreshButton.setOnClickListener { viewModel.refresh() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: WeatherUiState) {
        val loading = state is WeatherUiState.Loading
        binding.loadingGroup.visibility = if (loading) View.VISIBLE else View.GONE
        binding.refreshButton.isEnabled = !loading
        binding.weatherGroup.visibility = if (state is WeatherUiState.Success) View.VISIBLE else View.GONE
        binding.errorText.visibility = if (state is WeatherUiState.Error) View.VISIBLE else View.GONE
        errorBar?.dismiss()
        when (state) {
            is WeatherUiState.Success -> {
                val model = state.model
                binding.cityName.text = model.city
                binding.temperatureText.text = getString(R.string.temperature, model.temperature)
                binding.conditionText.text = getString(R.string.condition, model.condition.emoji, model.condition.description)
                binding.windText.text = getString(R.string.wind, model.windSpeed)
                binding.updatedText.text = getString(R.string.updated, model.updated)
                binding.forecastList.removeAllViews()
                model.days.forEach { day ->
                    val row = ItemForecastBinding.inflate(layoutInflater, binding.forecastList, false)
                    row.dateText.text = day.date
                    row.conditionText.text = getString(R.string.condition, day.condition.emoji, day.condition.description)
                    row.rangeText.text = getString(R.string.day_range, day.minimum, day.maximum)
                    binding.forecastList.addView(row.root)
                }
            }
            is WeatherUiState.Error -> {
                binding.errorText.text = state.message
                errorBar = Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.refresh) { viewModel.refresh() }.also { it.show() }
            }
            WeatherUiState.Loading -> Unit
        }
    }
}
