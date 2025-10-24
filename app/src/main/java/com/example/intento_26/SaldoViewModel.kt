package com.example.intento_26

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SaldoViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("finanzas_prefs", Context.MODE_PRIVATE)
    private val _saldo = MutableLiveData<Double>(prefs.getFloat("saldo", 0f).toDouble())
    val saldo: LiveData<Double> get() = _saldo

    fun registrarGasto(monto: Double) {
        val nuevoSaldo = (_saldo.value ?: 0.0) - monto
        _saldo.value = nuevoSaldo
        guardarSaldo(nuevoSaldo)
    }

    fun registrarAhorro(monto: Double) {
        val nuevoSaldo = (_saldo.value ?: 0.0) + monto
        _saldo.value = nuevoSaldo
        guardarSaldo(nuevoSaldo)
    }

    fun setSaldo(nuevoSaldo: Double) {
        _saldo.value = nuevoSaldo
        guardarSaldo(nuevoSaldo)
    }

    private fun guardarSaldo(saldo: Double) {
        prefs.edit().putFloat("saldo", saldo.toFloat()).apply()
    }
}

