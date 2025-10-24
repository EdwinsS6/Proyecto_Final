package com.example.intento_26;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GastosFragment extends Fragment {
    private List<Cuenta> cuentas;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gastos, container, false);
        cuentas = CuentaStorage.cargarCuentas(getContext());
        EditText etMonto = view.findViewById(R.id.et_monto_gasto);
        Button btnRegistrar = view.findViewById(R.id.btn_registrar_gasto);
        btnRegistrar.setOnClickListener(v -> {
            if (cuentas.isEmpty()) {
                new android.app.AlertDialog.Builder(getContext())
                    .setTitle("Sin cuentas")
                    .setMessage("Primero debes agregar una cuenta desde INICIO.")
                    .setPositiveButton("OK", null)
                    .show();
                return;
            }
            // Diálogo para seleccionar cuenta
            String[] nombres = new String[cuentas.size()];
            for (int i = 0; i < cuentas.size(); i++) nombres[i] = cuentas.get(i).nombre;
            final int[] seleccion = {0};
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("Selecciona la cuenta")
                .setSingleChoiceItems(nombres, 0, (dialog, which) -> seleccion[0] = which)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String montoStr = etMonto.getText().toString().trim();
                    EditText etDesc = getView().findViewById(R.id.et_desc_gasto);
                    String descStr = etDesc != null ? etDesc.getText().toString().trim() : "";
                    String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
                    if (!montoStr.isEmpty()) {
                        float monto = Float.parseFloat(montoStr);
                        Cuenta cuenta = cuentas.get(seleccion[0]);
                        cuenta.saldo -= monto;
                        CuentaStorage.guardarCuentas(getContext(), cuentas);
                        // Guardar el gasto en MovimientoStorage
                        MovimientoStorage.guardarGasto(getContext(), monto, descStr, fecha, cuenta.nombre);
                        etMonto.setText("");
                        if (etDesc != null) etDesc.setText("");
                        android.widget.Toast.makeText(getContext(), "Gasto registrado y saldo actualizado", android.widget.Toast.LENGTH_SHORT).show();
                        // Actualizar gráficas en InicioFragment si está visible
                        if (getActivity() != null) {
                            androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                            androidx.fragment.app.Fragment fragment = fm.findFragmentById(R.id.fragment_container);
                            if (fragment instanceof com.example.intento_26.InicioFragment) {
                                ((com.example.intento_26.InicioFragment) fragment).actualizarGraficas(fragment.getView());
                            }
                        }
                        // Mostrar historial de gastos
                        mostrarHistorialGastos();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mostrarHistorialGastos();
    }
    private void mostrarHistorialGastos() {
        View root = getView();
        if (root == null) return;

        android.widget.LinearLayout layout = root.findViewById(R.id.layout_historial_gastos);
        layout.removeAllViews();

        java.util.List<MovimientoStorage.Gasto> gastos = MovimientoStorage.obtenerGastos(getContext());

        if (gastos.isEmpty()) {
            android.widget.TextView tv = new android.widget.TextView(getContext());
            tv.setText("No hay gastos registrados aún");
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tv.setPadding(16, 16, 16, 16);
            layout.addView(tv);
            return;
        }

        // Mostrar los gastos en orden inverso (más recientes primero)
        for (int i = gastos.size() - 1; i >= 0; i--) {
            MovimientoStorage.Gasto gasto = gastos.get(i);

            // Inflar el layout del item de gasto
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_gasto_historial, layout, false);

            // Configurar los datos del gasto
            android.widget.TextView tvDescripcion = itemView.findViewById(R.id.tv_gasto_descripcion);
            android.widget.TextView tvMonto = itemView.findViewById(R.id.tv_gasto_monto);
            android.widget.TextView tvCuenta = itemView.findViewById(R.id.tv_gasto_cuenta);
            android.widget.TextView tvFecha = itemView.findViewById(R.id.tv_gasto_fecha);

            // Mostrar descripción o "Sin descripción"
            if (gasto.descripcion != null && !gasto.descripcion.trim().isEmpty()) {
                tvDescripcion.setText(gasto.descripcion);
            } else {
                tvDescripcion.setText("Gasto sin descripción");
                tvDescripcion.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            tvMonto.setText(String.format("-Q%.2f", gasto.monto));
            tvCuenta.setText(gasto.cuenta);
            tvFecha.setText(gasto.fecha);

            layout.addView(itemView);
        }
    }
}
