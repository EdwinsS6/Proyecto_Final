package com.example.intento_26;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etNombre = findViewById(R.id.et_nombre_registro);
        EditText etEmail = findViewById(R.id.et_email_registro);
        EditText etPassword = findViewById(R.id.et_password_registro);
        Button btnRegistrar = findViewById(R.id.btn_registrar);
        Button btnIrLogin = findViewById(R.id.btn_ir_login);

        btnRegistrar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            DBHelper dbHelper = new DBHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.rawQuery("SELECT id FROM usuarios WHERE email=?", new String[]{email});
            if (c.moveToFirst()) {
                Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put("nombre", nombre);
                values.put("email", email);
                values.put("password", password);
                db.insert("usuarios", null, values);
                Toast.makeText(this, "Registro exitoso. Inicia sesión.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            c.close();
        });

        btnIrLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}

