package com.example.intento_26;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {
    private List<Cuenta> cuentas;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        cuentas = CuentaStorage.cargarCuentas(getContext());

        Button btnAgregarCuenta = view.findViewById(R.id.btn_agregar_cuenta);
        btnAgregarCuenta.setOnClickListener(v -> mostrarDialogoAgregarCuenta());

        Button btnEnviarReporte = view.findViewById(R.id.btn_enviar_reporte_gmail);
        btnEnviarReporte.setOnClickListener(v -> mostrarDialogoEnviarReporte());

        actualizarGraficas(view);

        return view;
    }

    private void mostrarDialogoAgregarCuenta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Agregar cuenta");
        View dialogView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, null);
        EditText etNombre = new EditText(getContext());
        etNombre.setHint("Nombre de la cuenta");
        EditText etSaldo = new EditText(getContext());
        etSaldo.setHint("Saldo inicial");
        etSaldo.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(etNombre);
        layout.addView(etSaldo);
        builder.setView(layout);
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String saldoStr = etSaldo.getText().toString().trim();
            if (!nombre.isEmpty() && !saldoStr.isEmpty()) {
                float saldo = Float.parseFloat(saldoStr);
                cuentas.add(new Cuenta(nombre, saldo));
                CuentaStorage.guardarCuentas(getContext(), cuentas);
                actualizarGraficas(getView());
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoEnviarReporte() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enviar Reporte por Gmail");
        builder.setMessage("Ingresa el correo electrónico donde deseas recibir el reporte financiero:");

        EditText etEmail = new EditText(getContext());
        etEmail.setHint("ejemplo@gmail.com");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(etEmail);
        builder.setView(layout);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                enviarReportePorGmail(email);
            } else {
                Toast.makeText(getContext(), "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void enviarReportePorGmail(String email) {
        // Mostrar mensaje de progreso
        Toast.makeText(getContext(), "Preparando reporte...", Toast.LENGTH_SHORT).show();

        N8nReportService reportService = new N8nReportService(getContext());
        reportService.enviarReporte(email, new N8nReportService.ReportCallback() {
            @Override
            public void onSuccess(String message) {
                // Este callback se ejecuta en un hilo secundario, necesitamos ejecutar en el hilo principal
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "✓ " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "✗ " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    public void actualizarGraficas(View view) {
        if (view == null) return;

        // Recargar datos actualizados
        cuentas = CuentaStorage.cargarCuentas(getContext());

        // Calcular el saldo total real sumando los saldos de todas las cuentas
        float saldoTotal = 0;
        for (Cuenta c : cuentas) {
            saldoTotal += c.saldo;
        }

        // Actualizar el TextView del saldo total
        android.widget.TextView tvSaldo = view.findViewById(R.id.tv_saldo_total);
        if (tvSaldo != null) {
            tvSaldo.setText("Saldo total: Q" + String.format("%.2f", saldoTotal));
        }

        // Obtener y sumar todos los gastos registrados
        float totalGastos = 0;
        java.util.List<MovimientoStorage.Gasto> gastos = MovimientoStorage.obtenerGastos(getContext());
        for (MovimientoStorage.Gasto g : gastos) {
            totalGastos += g.monto;
        }

        // Construir reporte general
        StringBuilder reporte = new StringBuilder();
        reporte.append("📊 RESUMEN GENERAL\n\n");
        reporte.append("💰 Saldo actual: Q").append(String.format("%.2f", saldoTotal)).append("\n");
        reporte.append("💸 Total gastado: Q").append(String.format("%.2f", totalGastos)).append("\n");

        // Sumar ahorros
        float totalAhorros = 0;
        java.util.List<MovimientoStorage.Ahorro> ahorros = MovimientoStorage.obtenerAhorros(getContext());
        for (MovimientoStorage.Ahorro a : ahorros) {
            totalAhorros += a.monto;
        }
        reporte.append("💵 Total ahorrado: Q").append(String.format("%.2f", totalAhorros)).append("\n");

        // Sumar metas
        float totalMetasProgreso = 0;
        float totalMetasObjetivo = 0;
        java.util.List<Meta> metas = MetaStorage.cargarMetas(getContext());
        for (Meta m : metas) {
            totalMetasProgreso += m.progreso;
            totalMetasObjetivo += m.objetivo;
        }
        reporte.append("🎯 Progreso en metas: Q").append(String.format("%.2f", totalMetasProgreso));
        if (totalMetasObjetivo > 0) {
            float porcentajeMetas = (totalMetasProgreso / totalMetasObjetivo) * 100;
            reporte.append(" (").append(String.format("%.1f", porcentajeMetas)).append("%)");
        }
        reporte.append("\n");
        reporte.append("🏦 Cuentas activas: ").append(cuentas.size());

        android.widget.TextView tvReporte = view.findViewById(R.id.tv_reporte_general);
        if (tvReporte != null) {
            tvReporte.setText(reporte.toString());
        }

        // ========== GRÁFICA DE GASTOS (Mejorada) ==========
        com.github.mikephil.charting.charts.BarChart chartGastos = view.findViewById(R.id.chart_ingresos_gastos);

        if (totalGastos == 0) {
            chartGastos.clear();
            chartGastos.setNoDataText("No hay gastos registrados aún");
            chartGastos.invalidate();
        } else {
            java.util.ArrayList<com.github.mikephil.charting.data.BarEntry> entries = new java.util.ArrayList<>();
            entries.add(new com.github.mikephil.charting.data.BarEntry(0, totalGastos));

            com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries, "Total Gastado");
            dataSet.setColor(android.graphics.Color.rgb(244, 67, 54)); // Rojo para gastos
            dataSet.setValueTextSize(14f);
            dataSet.setValueTextColor(android.graphics.Color.BLACK);

            // Formato de valores en la barra
            com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
            barData.setBarWidth(0.5f);

            chartGastos.setData(barData);
            chartGastos.getDescription().setEnabled(false);
            chartGastos.setDrawGridBackground(false);
            chartGastos.setDrawBarShadow(false);
            chartGastos.setDrawValueAboveBar(true);
            chartGastos.setMaxVisibleValueCount(60);
            chartGastos.setPinchZoom(false);
            chartGastos.setDrawGridBackground(false);

            // Configurar eje X
            chartGastos.getXAxis().setEnabled(false);

            // Configurar eje Y izquierdo
            chartGastos.getAxisLeft().setDrawGridLines(false);
            chartGastos.getAxisLeft().setAxisMinimum(0f);

            // Configurar eje Y derecho
            chartGastos.getAxisRight().setEnabled(false);

            // Leyenda
            chartGastos.getLegend().setEnabled(true);
            chartGastos.getLegend().setTextSize(12f);

            chartGastos.animateY(1000);
            chartGastos.invalidate();
        }

        // ========== GRÁFICA DE AHORROS Y METAS (Mejorada) ==========
        com.github.mikephil.charting.charts.PieChart pieChart = view.findViewById(R.id.chart_ahorros_metas);

        if (totalAhorros == 0 && totalMetasProgreso == 0) {
            pieChart.clear();
            pieChart.setNoDataText("No hay ahorros ni metas registrados aún");
            pieChart.invalidate();
        } else {
            java.util.ArrayList<com.github.mikephil.charting.data.PieEntry> pieEntries = new java.util.ArrayList<>();

            if (totalAhorros > 0) {
                pieEntries.add(new com.github.mikephil.charting.data.PieEntry(totalAhorros, "Ahorros (Q" + String.format("%.2f", totalAhorros) + ")"));
            }
            if (totalMetasProgreso > 0) {
                pieEntries.add(new com.github.mikephil.charting.data.PieEntry(totalMetasProgreso, "Metas (Q" + String.format("%.2f", totalMetasProgreso) + ")"));
            }

            com.github.mikephil.charting.data.PieDataSet pieDataSet = new com.github.mikephil.charting.data.PieDataSet(pieEntries, "");

            // Colores profesionales
            java.util.ArrayList<Integer> colors = new java.util.ArrayList<>();
            colors.add(android.graphics.Color.rgb(76, 175, 80));   // Verde para ahorros
            colors.add(android.graphics.Color.rgb(255, 152, 0));   // Naranja para metas
            pieDataSet.setColors(colors);

            pieDataSet.setValueTextSize(14f);
            pieDataSet.setValueTextColor(android.graphics.Color.WHITE);
            pieDataSet.setSliceSpace(3f);

            com.github.mikephil.charting.data.PieData pieData = new com.github.mikephil.charting.data.PieData(pieDataSet);

            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(android.graphics.Color.TRANSPARENT);
            pieChart.setHoleRadius(40f);
            pieChart.setTransparentCircleRadius(45f);
            pieChart.setDrawCenterText(true);
            pieChart.setCenterText("Ahorros\ny\nMetas");
            pieChart.setCenterTextSize(14f);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);

            // Leyenda
            pieChart.getLegend().setEnabled(true);
            pieChart.getLegend().setTextSize(12f);
            pieChart.getLegend().setWordWrapEnabled(true);

            pieChart.animateY(1000);
            pieChart.invalidate();
        }
    }
}
