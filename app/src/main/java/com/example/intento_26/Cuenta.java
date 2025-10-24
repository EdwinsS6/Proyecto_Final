package com.example.intento_26;

import org.json.JSONException;
import org.json.JSONObject;

public class Cuenta {
    public String nombre;
    public float saldo;

    public Cuenta(String nombre, float saldo) {
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("nombre", nombre);
        obj.put("saldo", saldo);
        return obj;
    }

    public static Cuenta fromJson(JSONObject obj) throws JSONException {
        return new Cuenta(obj.getString("nombre"), (float) obj.getDouble("saldo"));
    }
}

