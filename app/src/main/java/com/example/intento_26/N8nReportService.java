package com.example.intento_26;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class N8nReportService {
    private static final String TAG = "N8nReportService";

    // URL del webhook de n8n
    private static final String N8N_WEBHOOK_URL = "https://primary-production-78316.up.railway.app/webhook/resumen_finanzas";


    private final Context context;
    private final OkHttpClient client;

    public N8nReportService(Context context) {
        this.context = context;
        this.client = new OkHttpClient();
    }

    public interface ReportCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void enviarReporte(String emailDestino, ReportCallback callback) {
        try {
            JSONObject reportData = generarReporteCompleto();
            reportData.put("email", emailDestino);

            // Log para debugging - ver qué datos se están enviando
            Log.d(TAG, "JSON enviado a n8n: " + reportData.toString());

            // Crear el request body
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, reportData.toString());

            // Crear el request con headers
            Request request = new Request.Builder()
                    .url(N8N_WEBHOOK_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error al enviar reporte: " + e.getMessage());
                    callback.onError("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Reporte enviado exitosamente");
                        callback.onSuccess("Reporte enviado correctamente a " + emailDestino);
                    } else {
                        Log.e(TAG, "Error en respuesta: " + response.code());
                        callback.onError("Error del servidor: " + response.code());
                    }
                    response.close();
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error al generar JSON: " + e.getMessage());
            callback.onError("Error al preparar los datos");
        }
    }

    private JSONObject generarReporteCompleto() throws JSONException {
        JSONObject report = new JSONObject();

        // Obtener fecha actual
        String fechaActual = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()).format(new java.util.Date());
        report.put("fechaGeneracion", fechaActual);

        // 1. CUENTAS
        List<Cuenta> cuentas = CuentaStorage.cargarCuentas(context);
        Log.d(TAG, "📊 Cuentas cargadas: " + cuentas.size());
        JSONArray cuentasArray = new JSONArray();
        float saldoTotal = 0;

        for (Cuenta cuenta : cuentas) {
            Log.d(TAG, "  💳 " + cuenta.nombre + ": Q" + cuenta.saldo);
            JSONObject cuentaObj = new JSONObject();
            cuentaObj.put("nombre", cuenta.nombre);
            cuentaObj.put("saldo", cuenta.saldo);
            cuentasArray.put(cuentaObj);
            saldoTotal += cuenta.saldo;
        }
        report.put("cuentas", cuentasArray);
        report.put("saldoTotal", saldoTotal);
        Log.d(TAG, "💰 Saldo Total: Q" + saldoTotal);

        // 2. GASTOS
        List<MovimientoStorage.Gasto> gastos = MovimientoStorage.obtenerGastos(context);
        Log.d(TAG, "📊 Gastos cargados: " + gastos.size());
        JSONArray gastosArray = new JSONArray();
        float totalGastos = 0;

        for (MovimientoStorage.Gasto gasto : gastos) {
            Log.d(TAG, "  💸 " + gasto.descripcion + ": Q" + gasto.monto + " [" + gasto.fecha + "]");
            JSONObject gastoObj = new JSONObject();
            gastoObj.put("monto", gasto.monto);
            gastoObj.put("descripcion", gasto.descripcion);
            gastoObj.put("fecha", gasto.fecha);
            gastoObj.put("cuenta", gasto.cuenta);
            gastosArray.put(gastoObj);
            totalGastos += gasto.monto;
        }
        report.put("gastos", gastosArray);
        report.put("totalGastos", totalGastos);
        Log.d(TAG, "💸 Total Gastos: Q" + totalGastos);

        // 3. AHORROS
        List<MovimientoStorage.Ahorro> ahorros = MovimientoStorage.obtenerAhorros(context);
        Log.d(TAG, "📊 Ahorros cargados: " + ahorros.size());
        JSONArray ahorrosArray = new JSONArray();
        float totalAhorros = 0;

        for (MovimientoStorage.Ahorro ahorro : ahorros) {
            Log.d(TAG, "  💰 " + ahorro.descripcion + ": Q" + ahorro.monto + " [" + ahorro.fecha + "]");
            JSONObject ahorroObj = new JSONObject();
            ahorroObj.put("monto", ahorro.monto);
            ahorroObj.put("descripcion", ahorro.descripcion);
            ahorroObj.put("fecha", ahorro.fecha);
            ahorroObj.put("cuenta", ahorro.cuenta);
            ahorrosArray.put(ahorroObj);
            totalAhorros += ahorro.monto;
        }
        report.put("ahorros", ahorrosArray);
        report.put("totalAhorros", totalAhorros);
        Log.d(TAG, "💰 Total Ahorros: Q" + totalAhorros);

        // 4. METAS
        List<Meta> metas = MetaStorage.cargarMetas(context);
        Log.d(TAG, "📊 Metas cargadas: " + metas.size());
        JSONArray metasArray = new JSONArray();
        float totalMetasProgreso = 0;
        float totalMetasObjetivo = 0;

        for (Meta meta : metas) {
            float porcentaje = (meta.progreso / meta.objetivo) * 100;
            Log.d(TAG, "  🎯 " + meta.nombre + ": Q" + meta.progreso + "/Q" + meta.objetivo + " (" + String.format("%.1f", porcentaje) + "%)");
            JSONObject metaObj = new JSONObject();
            metaObj.put("nombre", meta.nombre);
            metaObj.put("objetivo", meta.objetivo);
            metaObj.put("progreso", meta.progreso);
            metaObj.put("fechaLimite", meta.fechaLimite);
            metaObj.put("porcentaje", porcentaje);
            metasArray.put(metaObj);
            totalMetasProgreso += meta.progreso;
            totalMetasObjetivo += meta.objetivo;
        }
        report.put("metas", metasArray);
        report.put("totalMetasProgreso", totalMetasProgreso);
        report.put("totalMetasObjetivo", totalMetasObjetivo);
        Log.d(TAG, "🎯 Total Metas Progreso: Q" + totalMetasProgreso + " / Q" + totalMetasObjetivo);

        // 5. RESUMEN
        JSONObject resumen = new JSONObject();
        resumen.put("numeroCuentas", cuentas.size());
        resumen.put("numeroGastos", gastos.size());
        resumen.put("numeroAhorros", ahorros.size());
        resumen.put("numeroMetas", metas.size());
        resumen.put("balanceGeneral", saldoTotal - totalGastos + totalAhorros);
        report.put("resumen", resumen);

        return report;
    }
}

