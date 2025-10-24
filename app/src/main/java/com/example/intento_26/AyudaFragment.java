package com.example.intento_26;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AyudaFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ayuda, container, false);
        Button btnWhatsapp = view.findViewById(R.id.btn_whatsapp);
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = "50242216230"; // Cambia por el número de soporte con código de país
                String mensaje = "Hola, necesito ayuda con la app."; // Mensaje predeterminado
                String url = "https://wa.me/" + phoneNumber + "?text=" + Uri.encode(mensaje);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setPackage("com.whatsapp");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "WhatsApp no está instalado", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
