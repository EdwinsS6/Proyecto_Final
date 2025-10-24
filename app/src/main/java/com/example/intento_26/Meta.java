package com.example.intento_26;

import org.json.JSONException;
import org.json.JSONObject;

public class Meta {
    public String nombre;
    public float objetivo;
    public float progreso;
    public String fechaLimite;

    public Meta(String nombre, float objetivo, float progreso, String fechaLimite) {
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.progreso = progreso;
        this.fechaLimite = fechaLimite;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("nombre", nombre);
        obj.put("objetivo", objetivo);
        obj.put("progreso", progreso);
        obj.put("fechaLimite", fechaLimite);
        return obj;
    }

    public static Meta fromJson(JSONObject obj) throws JSONException {
        return new Meta(
            obj.getString("nombre"),
            (float) obj.getDouble("objetivo"),
            (float) obj.getDouble("progreso"),
            obj.getString("fechaLimite")
        );
    }
}

