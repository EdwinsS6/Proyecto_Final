package com.example.intento_26;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CuentaStorage {
    private static final String PREFS_NAME = "cuentas_prefs";
    private static final String KEY_CUENTAS = "cuentas";

    public static void guardarCuentas(Context context, List<Cuenta> cuentas) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (Cuenta c : cuentas) {
            try {
                arr.put(c.toJson());
            } catch (JSONException e) { }
        }
        prefs.edit().putString(KEY_CUENTAS, arr.toString()).apply();
    }

    public static List<Cuenta> cargarCuentas(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CUENTAS, null);
        List<Cuenta> cuentas = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    cuentas.add(Cuenta.fromJson(arr.getJSONObject(i)));
                }
            } catch (JSONException e) { }
        }
        return cuentas;
    }
}

