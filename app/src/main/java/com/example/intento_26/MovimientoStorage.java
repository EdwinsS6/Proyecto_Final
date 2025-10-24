package com.example.intento_26;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MovimientoStorage {
    private static final String PREFS_NAME = "movimientos_prefs";
    private static final String KEY_GASTOS = "gastos";
    private static final String KEY_AHORROS = "ahorros";

    public static void guardarGasto(Context context, float monto, String descripcion, String fecha, String cuenta) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_GASTOS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("monto", monto);
            obj.put("descripcion", descripcion);
            obj.put("fecha", fecha);
            obj.put("cuenta", cuenta);
            arr.put(obj);
            prefs.edit().putString(KEY_GASTOS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<Gasto> obtenerGastos(Context context) {
        List<Gasto> lista = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_GASTOS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                float monto = (float) obj.getDouble("monto");
                String descripcion = obj.optString("descripcion", "");
                String fecha = obj.optString("fecha", "");
                String cuenta = obj.optString("cuenta", "");
                lista.add(new Gasto(monto, descripcion, fecha, cuenta));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static void guardarAhorro(Context context, float monto, String descripcion, String fecha, String cuenta) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_AHORROS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            JSONObject obj = new JSONObject();
            obj.put("monto", monto);
            obj.put("descripcion", descripcion);
            obj.put("fecha", fecha);
            obj.put("cuenta", cuenta);
            arr.put(obj);
            prefs.edit().putString(KEY_AHORROS, arr.toString()).apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<Ahorro> obtenerAhorros(Context context) {
        List<Ahorro> lista = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_AHORROS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                float monto = (float) obj.getDouble("monto");
                String descripcion = obj.optString("descripcion", "");
                String fecha = obj.optString("fecha", "");
                String cuenta = obj.optString("cuenta", "");
                lista.add(new Ahorro(monto, descripcion, fecha, cuenta));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Clase auxiliar para representar un gasto
    public static class Gasto {
        public float monto;
        public String descripcion;
        public String fecha;
        public String cuenta;
        public Gasto(float monto, String descripcion, String fecha, String cuenta) {
            this.monto = monto;
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.cuenta = cuenta;
        }
    }

    // Clase auxiliar para representar un ahorro
    public static class Ahorro {
        public float monto;
        public String descripcion;
        public String fecha;
        public String cuenta;
        public Ahorro(float monto, String descripcion, String fecha, String cuenta) {
            this.monto = monto;
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.cuenta = cuenta;
        }
    }
}
