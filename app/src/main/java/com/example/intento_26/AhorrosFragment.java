package com.example.intento_26;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AhorrosFragment extends Fragment {
    private List<Cuenta> cuentas;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ahorros, container, false);
        cuentas = CuentaStorage.cargarCuentas(getContext());
        EditText etMonto = view.findViewById(R.id.et_monto_ahorro);
        Button btnRegistrar = view.findViewById(R.id.btn_registrar_ahorro);
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
                    EditText etDesc = getView().findViewById(R.id.et_desc_ahorro);
                    String descStr = etDesc != null ? etDesc.getText().toString().trim() : "";
                    String fecha = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
                    if (!montoStr.isEmpty()) {
                        float monto = Float.parseFloat(montoStr);
                        Cuenta cuenta = cuentas.get(seleccion[0]);
                        cuenta.saldo += monto;
                        CuentaStorage.guardarCuentas(getContext(), cuentas);
                        // Guardar el ahorro en MovimientoStorage
                        MovimientoStorage.guardarAhorro(getContext(), monto, descStr, fecha, cuenta.nombre);
                        etMonto.setText("");
                        if (etDesc != null) etDesc.setText("");
                        android.widget.Toast.makeText(getContext(), "Ahorro registrado y saldo actualizado", android.widget.Toast.LENGTH_SHORT).show();
                        // Actualizar gráficas en InicioFragment si está visible
                        if (getActivity() != null) {
                            androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                            androidx.fragment.app.Fragment fragment = fm.findFragmentById(R.id.fragment_container);
                            if (fragment instanceof com.example.intento_26.InicioFragment) {
                                ((com.example.intento_26.InicioFragment) fragment).actualizarGraficas(fragment.getView());
                            }
                        }
                        // Mostrar historial de ahorros
                        mostrarHistorialAhorros();
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
        mostrarHistorialAhorros();
    }

    private void mostrarHistorialAhorros() {
        View root = getView();
        if (root == null) return;

        android.widget.LinearLayout layout = root.findViewById(R.id.layout_historial_ahorros);
        layout.removeAllViews();

        java.util.List<MovimientoStorage.Ahorro> ahorros = MovimientoStorage.obtenerAhorros(getContext());

        if (ahorros.isEmpty()) {
            android.widget.TextView tv = new android.widget.TextView(getContext());
            tv.setText("No hay ahorros registrados aún");
            tv.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tv.setPadding(16, 16, 16, 16);
            layout.addView(tv);
            return;
        }

        // Mostrar los ahorros en orden inverso (más recientes primero)
        for (int i = ahorros.size() - 1; i >= 0; i--) {
            MovimientoStorage.Ahorro ahorro = ahorros.get(i);

            // Inflar el layout del item de ahorro
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_ahorro_historial, layout, false);

            // Configurar los datos del ahorro
            android.widget.TextView tvDescripcion = itemView.findViewById(R.id.tv_ahorro_descripcion);
            android.widget.TextView tvMonto = itemView.findViewById(R.id.tv_ahorro_monto);
            android.widget.TextView tvCuenta = itemView.findViewById(R.id.tv_ahorro_cuenta);
            android.widget.TextView tvFecha = itemView.findViewById(R.id.tv_ahorro_fecha);

            // Mostrar descripción o "Sin descripción"
            if (ahorro.descripcion != null && !ahorro.descripcion.trim().isEmpty()) {
                tvDescripcion.setText(ahorro.descripcion);
            } else {
                tvDescripcion.setText("Ahorro sin descripción");
                tvDescripcion.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }

            tvMonto.setText(String.format("+Q%.2f", ahorro.monto));
            tvCuenta.setText(ahorro.cuenta);
            tvFecha.setText(ahorro.fecha);

            layout.addView(itemView);
        }
    }
}
