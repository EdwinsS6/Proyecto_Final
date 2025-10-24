package com.example.intento_26;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;

public class MetaStorage {
    private static final String PREFS_NAME = "metas_prefs";
    private static final String KEY_METAS = "metas";

    public static void guardarMetas(Context context, List<Meta> metas) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (Meta m : metas) {
            try {
                arr.put(m.toJson());
            } catch (JSONException e) { }
        }
        prefs.edit().putString(KEY_METAS, arr.toString()).apply();
    }

    public static List<Meta> cargarMetas(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_METAS, null);
        List<Meta> metas = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray arr = new JSONArray(json);
                for (int i = 0; i < arr.length(); i++) {
                    metas.add(Meta.fromJson(arr.getJSONObject(i)));
                }
            } catch (JSONException e) { }
        }
        return metas;
    }
}

