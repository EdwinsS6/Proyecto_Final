package com.example.intento_26;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.Calendar;
import java.util.List;

public class MetasFragment extends Fragment {
    private List<Cuenta> cuentas;
    private List<Meta> metas;
    private ArrayAdapter<String> metasAdapter;
    private ListView listViewMetas;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metas, container, false);
        cuentas = CuentaStorage.cargarCuentas(getContext());
        metas = MetaStorage.cargarMetas(getContext());
        // ListView para mostrar metas
        listViewMetas = new ListView(getContext());
        ((android.widget.LinearLayout)((android.widget.ScrollView)view).getChildAt(0)).addView(listViewMetas, 6); // Insertar después de los botones
        metasAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getMetasResumen()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                android.widget.TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(android.R.color.black));
                return view;
            }
        };
        listViewMetas.setAdapter(metasAdapter);
        // Botón agregar meta
        Button btnAgregarMeta = view.findViewById(R.id.btn_agregar_meta);
        btnAgregarMeta.setOnClickListener(v -> mostrarDialogoAgregarMeta());
        // Botón aportar a meta
        Button btnAportar = view.findViewById(R.id.btn_aportar_meta);
        btnAportar.setOnClickListener(v -> mostrarDialogoAportarMeta());
        actualizarGraficaMetas(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mostrarHistorialMetas();
    }


    private void mostrarDialogoAgregarMeta() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Agregar meta");
        EditText etNombre = new EditText(getContext());
        etNombre.setHint("Nombre de la meta");
        EditText etObjetivo = new EditText(getContext());
        etObjetivo.setHint("Monto objetivo");
        etObjetivo.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText etFecha = new EditText(getContext());
        etFecha.setHint("Fecha límite (dd/mm/yyyy)");
        etFecha.setFocusable(false);
        etFecha.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, day) -> {
                String fecha = String.format("%02d/%02d/%04d", day, month+1, year);
                etFecha.setText(fecha);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(etNombre);
        layout.addView(etObjetivo);
        layout.addView(etFecha);
        builder.setView(layout);
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = etNombre.getText().toString().trim();
            String objetivoStr = etObjetivo.getText().toString().trim();
            String fecha = etFecha.getText().toString().trim();
            if (!nombre.isEmpty() && !objetivoStr.isEmpty() && !fecha.isEmpty()) {
                float objetivo = Float.parseFloat(objetivoStr);
                // Agregar la meta sin descontar del saldo
                metas.add(new Meta(nombre, objetivo, 0, fecha));
                MetaStorage.guardarMetas(getContext(), metas);

                // Recargar todas las metas desde el almacenamiento para asegurar que se vean todas
                mostrarHistorialMetas();
                metas = MetaStorage.cargarMetas(getContext());

                metasAdapter.clear();
                metasAdapter.addAll(getMetasResumen());
                actualizarGraficaMetas(getView());
                android.widget.Toast.makeText(getContext(), "Meta agregada exitosamente", android.widget.Toast.LENGTH_SHORT).show();
                // Actualizar gráficas en InicioFragment si está visible
                if (getActivity() != null) {
                    androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                    androidx.fragment.app.Fragment fragment = fm.findFragmentById(R.id.fragment_container);
                mostrarHistorialMetas();
                    if (fragment instanceof com.example.intento_26.InicioFragment) {
                        ((com.example.intento_26.InicioFragment) fragment).actualizarGraficas(fragment.getView());
                    }
                }
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDialogoAportarMeta() {
        if (metas.isEmpty()) {
            new AlertDialog.Builder(getContext())
                .setTitle("Sin metas")
                .setMessage("Primero debes agregar una meta.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        if (cuentas.isEmpty()) {
            new AlertDialog.Builder(getContext())
                .setTitle("Sin cuentas")
                .setMessage("Primero debes agregar una cuenta desde INICIO.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }
        String[] nombresMetas = new String[metas.size()];
        for (int i = 0; i < metas.size(); i++) nombresMetas[i] = metas.get(i).nombre;
        String[] nombresCuentas = new String[cuentas.size()];
        for (int i = 0; i < cuentas.size(); i++) nombresCuentas[i] = cuentas.get(i).nombre;
        final int[] seleccionMeta = {0};
        final int[] seleccionCuenta = {0};
        EditText etMonto = new EditText(getContext());
        etMonto.setHint("Monto a aportar");
        etMonto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.addView(etMonto);
        new AlertDialog.Builder(getContext())
            .setTitle("Selecciona la meta")
            .setSingleChoiceItems(nombresMetas, 0, (dialog, which) -> seleccionMeta[0] = which)
            .setPositiveButton("Siguiente", (dialog, which) -> {
                new AlertDialog.Builder(getContext())
                    .setTitle("Selecciona la cuenta")
                    .setSingleChoiceItems(nombresCuentas, 0, (dialog2, which2) -> seleccionCuenta[0] = which2)
                    .setView(layout)
                    .setPositiveButton("Aceptar", (dialog2, which2) -> {
                        String montoStr = etMonto.getText().toString().trim();
                        if (!montoStr.isEmpty()) {
                            float monto = Float.parseFloat(montoStr);
                            Meta meta = metas.get(seleccionMeta[0]);
                            Cuenta cuenta = cuentas.get(seleccionCuenta[0]);
                            if (monto > 0 && cuenta.saldo >= monto) {
                                meta.progreso += monto;
                                cuenta.saldo -= monto;
                                MetaStorage.guardarMetas(getContext(), metas);
                                CuentaStorage.guardarCuentas(getContext(), cuentas);
                                metasAdapter.clear();
                                metasAdapter.addAll(getMetasResumen());
                                metasAdapter.notifyDataSetChanged();
                                actualizarGraficaMetas(getView());
                                mostrarHistorialMetas();
                                android.widget.Toast.makeText(getContext(), "Aporte registrado", android.widget.Toast.LENGTH_SHORT).show();
                            } else {
                                android.widget.Toast.makeText(getContext(), "Monto inválido o saldo insuficiente", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private ArrayList<String> getMetasResumen() {
        ArrayList<String> resumen = new ArrayList<>();
        for (Meta m : metas) {
            resumen.add(m.nombre + " | Objetivo: Q" + String.format("%.2f", m.objetivo) +
                " | Progreso: Q" + String.format("%.2f", m.progreso) +
                " | Fecha límite: " + m.fechaLimite);
        }
        return resumen;
    }

    private void actualizarGraficaMetas(View view) {
        PieChart chart = view.findViewById(R.id.chart_metas);
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (metas.isEmpty()) {
            chart.clear();
            chart.setNoDataText("No hay metas registradas");
            return;
        }

        for (Meta m : metas) {
            float porcentaje = m.objetivo > 0 ? (m.progreso / m.objetivo) * 100f : 0f;
            entries.add(new PieEntry(porcentaje, m.nombre));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Progreso Metas");

        // Agregar colores diferentes para cada meta
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(android.graphics.Color.rgb(255, 99, 132));   // Rojo/Rosa
        colors.add(android.graphics.Color.rgb(54, 162, 235));   // Azul
        colors.add(android.graphics.Color.rgb(255, 206, 86));   // Amarillo
        colors.add(android.graphics.Color.rgb(75, 192, 192));   // Verde agua
        colors.add(android.graphics.Color.rgb(153, 102, 255));  // Morado
        colors.add(android.graphics.Color.rgb(255, 159, 64));   // Naranja
        colors.add(android.graphics.Color.rgb(199, 199, 199));  // Gris
        colors.add(android.graphics.Color.rgb(83, 102, 255));   // Azul índigo
        colors.add(android.graphics.Color.rgb(255, 99, 255));   // Magenta
        colors.add(android.graphics.Color.rgb(99, 255, 132));   // Verde claro

        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        PieData pieData = new PieData(dataSet);
        chart.setData(pieData);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(android.graphics.Color.TRANSPARENT);
        chart.setTransparentCircleRadius(58f);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void mostrarHistorialMetas() {
        View root = getView();
        if (root == null) return;

        // Recargar las metas desde el almacenamiento para asegurar que se muestren todas
        metas = MetaStorage.cargarMetas(getContext());

        android.widget.LinearLayout layout = root.findViewById(R.id.layout_historial_metas);
        layout.removeAllViews();

        if (metas.isEmpty()) {
            android.widget.TextView tv = new android.widget.TextView(getContext());
            tv.setText("No hay metas registradas aún");
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tv.setPadding(16, 16, 16, 16);
            layout.addView(tv);
            return;
        }

        for (int i = 0; i < metas.size(); i++) {
            final Meta meta = metas.get(i);
            final int metaIndex = i;

            // Inflar el layout del item de meta
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_meta_historial, layout, false);

            // Configurar los datos de la meta
            android.widget.TextView tvNombre = itemView.findViewById(R.id.tv_meta_nombre);
            android.widget.TextView tvObjetivo = itemView.findViewById(R.id.tv_meta_objetivo);
            android.widget.TextView tvProgreso = itemView.findViewById(R.id.tv_meta_progreso);
            android.widget.TextView tvFecha = itemView.findViewById(R.id.tv_meta_fecha);
            android.widget.TextView tvPorcentaje = itemView.findViewById(R.id.tv_meta_porcentaje);
            android.widget.ProgressBar progressBar = itemView.findViewById(R.id.progress_meta);
            android.widget.Button btnAportar = itemView.findViewById(R.id.btn_aportar_meta);

            tvNombre.setText(meta.nombre);
            tvObjetivo.setText(String.format("Q%.2f", meta.objetivo));
            tvProgreso.setText(String.format("Q%.2f", meta.progreso));
            tvFecha.setText(meta.fechaLimite);

            // Calcular porcentaje
            float porcentaje = meta.objetivo > 0 ? (meta.progreso / meta.objetivo) * 100f : 0f;
            if (porcentaje > 100f) porcentaje = 100f;

            progressBar.setProgress((int) porcentaje);
            tvPorcentaje.setText(String.format("%.1f%% completado", porcentaje));

            // Cambiar color si está completada
            if (porcentaje >= 100f) {
                tvProgreso.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                btnAportar.setEnabled(false);
                btnAportar.setText("Meta completada ✓");
            }

            // Listener para el botón de aportar
            btnAportar.setOnClickListener(v -> mostrarDialogoAportarAMetaDirecta(meta, metaIndex));

            layout.addView(itemView);
        }
    }

    private void mostrarDialogoAportarAMetaDirecta(final Meta meta, final int metaIndex) {
        // Recargar cuentas
        cuentas = CuentaStorage.cargarCuentas(getContext());

        if (cuentas.isEmpty()) {
            new AlertDialog.Builder(getContext())
                .setTitle("Sin cuentas")
                .setMessage("Primero debes agregar una cuenta desde INICIO.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        // Verificar si la meta ya está completa
        if (meta.progreso >= meta.objetivo) {
            new AlertDialog.Builder(getContext())
                .setTitle("Meta completada")
                .setMessage("Esta meta ya ha alcanzado su objetivo.")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Aportar a: " + meta.nombre);

        float faltante = meta.objetivo - meta.progreso;

        android.widget.LinearLayout dialogLayout = new android.widget.LinearLayout(getContext());
        dialogLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        dialogLayout.setPadding(50, 40, 50, 10);

        android.widget.TextView tvInfo = new android.widget.TextView(getContext());
        tvInfo.setText(String.format("Progreso: Q%.2f / Q%.2f\nFaltante: Q%.2f",
            meta.progreso, meta.objetivo, faltante));
        tvInfo.setPadding(0, 0, 0, 20);
        tvInfo.setTextColor(getResources().getColor(android.R.color.black));
        dialogLayout.addView(tvInfo);

        EditText etMonto = new EditText(getContext());
        etMonto.setHint("Monto a aportar");
        etMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        dialogLayout.addView(etMonto);

        builder.setView(dialogLayout);

        // Seleccionar cuenta
        String[] nombresCuentas = new String[cuentas.size()];
        for (int i = 0; i < cuentas.size(); i++) {
            nombresCuentas[i] = cuentas.get(i).nombre + " (Saldo: Q" +
                String.format("%.2f", cuentas.get(i).saldo) + ")";
        }

        final int[] seleccionCuenta = {0};

        builder.setPositiveButton("Siguiente", null);
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String montoStr = etMonto.getText().toString().trim();
            if (montoStr.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Ingresa un monto", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            float monto = Float.parseFloat(montoStr);
            if (monto <= 0) {
                android.widget.Toast.makeText(getContext(), "El monto debe ser mayor a 0", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();

            // Mostrar diálogo para seleccionar cuenta
            new AlertDialog.Builder(getContext())
                .setTitle("Selecciona la cuenta")
                .setSingleChoiceItems(nombresCuentas, 0, (d, which) -> seleccionCuenta[0] = which)
                .setPositiveButton("Aportar", (d, which) -> {
                    Cuenta cuenta = cuentas.get(seleccionCuenta[0]);

                    if (cuenta.saldo < monto) {
                        new AlertDialog.Builder(getContext())
                            .setTitle("Saldo insuficiente")
                            .setMessage(String.format("La cuenta %s solo tiene Q%.2f disponible.",
                                cuenta.nombre, cuenta.saldo))
                            .setPositiveButton("OK", null)
                            .show();
                        return;
                    }

                    // Descontar de la cuenta
                    cuenta.saldo -= monto;
                    CuentaStorage.guardarCuentas(getContext(), cuentas);

                    // Agregar al progreso de la meta
                    meta.progreso += monto;
                    metas.set(metaIndex, meta);
                    MetaStorage.guardarMetas(getContext(), metas);

                    // Guardar el movimiento en el historial
                    String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
                    MovimientoStorage.guardarAhorro(getContext(), monto,
                        "Aporte a meta: " + meta.nombre, fecha, cuenta.nombre);

                    // Actualizar todas las vistas
                    metasAdapter.clear();
                    metasAdapter.addAll(getMetasResumen());
                    metasAdapter.notifyDataSetChanged();
                    actualizarGraficaMetas(getView());
                    mostrarHistorialMetas();

                    // Mostrar mensaje de éxito
                    String mensaje = String.format("Se aportó Q%.2f a la meta '%s'\nSaldo restante en %s: Q%.2f",
                        monto, meta.nombre, cuenta.nombre, cuenta.saldo);

                    if (meta.progreso >= meta.objetivo) {
                        mensaje += "\n\n🎉 ¡Felicidades! Meta completada.";
                    }

                    new AlertDialog.Builder(getContext())
                        .setTitle("Aporte registrado")
                        .setMessage(mensaje)
                        .setPositiveButton("OK", null)
                        .show();

                    // Actualizar gráficas en InicioFragment si está visible
                    if (getActivity() != null) {
                        androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                        androidx.fragment.app.Fragment fragment = fm.findFragmentById(R.id.fragment_container);
                        if (fragment instanceof InicioFragment) {
                            ((InicioFragment) fragment).actualizarGraficas(fragment.getView());
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
    }
}
