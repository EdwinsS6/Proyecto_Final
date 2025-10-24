package com.example.intento_26;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "finanzas.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, email TEXT, password TEXT)");
        db.execSQL("CREATE TABLE cuentas (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_id INTEGER, nombre TEXT, saldo REAL, moneda TEXT, FOREIGN KEY(usuario_id) REFERENCES usuarios(id))");
        db.execSQL("CREATE TABLE gastos (id INTEGER PRIMARY KEY AUTOINCREMENT, cuenta_id INTEGER, monto REAL, moneda TEXT, descripcion TEXT, fecha TEXT, FOREIGN KEY(cuenta_id) REFERENCES cuentas(id))");
        db.execSQL("CREATE TABLE ahorros (id INTEGER PRIMARY KEY AUTOINCREMENT, cuenta_id INTEGER, monto REAL, moneda TEXT, descripcion TEXT, fecha TEXT, FOREIGN KEY(cuenta_id) REFERENCES cuentas(id))");
        db.execSQL("CREATE TABLE metas (id INTEGER PRIMARY KEY AUTOINCREMENT, cuenta_id INTEGER, monto_objetivo REAL, moneda TEXT, descripcion TEXT, fecha_limite TEXT, monto_actual REAL DEFAULT 0, completada INTEGER DEFAULT 0, FOREIGN KEY(cuenta_id) REFERENCES cuentas(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS cuentas");
        db.execSQL("DROP TABLE IF EXISTS gastos");
        db.execSQL("DROP TABLE IF EXISTS ahorros");
        db.execSQL("DROP TABLE IF EXISTS metas");
        onCreate(db);
    }
}

