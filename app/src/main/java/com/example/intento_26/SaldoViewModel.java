package com.example.intento_26;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SaldoViewModel extends AndroidViewModel {
    private final Context context;
    private final MutableLiveData<Double> saldo;

    public SaldoViewModel(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
        float saldoGuardado = context.getSharedPreferences("finanzas_prefs", Context.MODE_PRIVATE)
                .getFloat("saldo", 0f);
        saldo = new MutableLiveData<>((double) saldoGuardado);
    }

    public LiveData<Double> getSaldo() {
        return saldo;
    }

    public void registrarGasto(double monto) {
        double nuevoSaldo = (saldo.getValue() != null ? saldo.getValue() : 0.0) - monto;
        saldo.setValue(nuevoSaldo);
        guardarSaldo(nuevoSaldo);
    }

    public void registrarAhorro(double monto) {
        double nuevoSaldo = (saldo.getValue() != null ? saldo.getValue() : 0.0) + monto;
        saldo.setValue(nuevoSaldo);
        guardarSaldo(nuevoSaldo);
    }

    public void setSaldo(double nuevoSaldo) {
        saldo.setValue(nuevoSaldo);
        guardarSaldo(nuevoSaldo);
    }

    private void guardarSaldo(double saldo) {
        context.getSharedPreferences("finanzas_prefs", Context.MODE_PRIVATE)
                .edit().putFloat("saldo", (float) saldo).apply();
    }
}

