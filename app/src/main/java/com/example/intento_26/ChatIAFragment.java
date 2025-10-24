package com.example.intento_26;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.intento_26.ChatAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class ChatIAFragment extends Fragment {
    private ChatAdapter adapter;
    private ArrayList<String> chatList;
    // Cambia aquí tu API Key de DeepSeek
    private static final String API_KEY = "sk-effa808d344e4584a6fe90464eb93a8e";
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_ia, container, false);
        EditText etMensaje = view.findViewById(R.id.et_mensaje);
        Button btnEnviar = view.findViewById(R.id.btn_enviar);
        ListView listChat = view.findViewById(R.id.list_chat);
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(getContext(), chatList);
        listChat.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> {
            String mensaje = etMensaje.getText().toString().trim();
            if (mensaje.isEmpty()) return;

            // Agregar el mensaje del usuario
            chatList.add("Tú: " + mensaje);
            adapter.notifyDataSetChanged();
            etMensaje.setText("");

            // Scroll al final de la lista
            listChat.post(() -> listChat.setSelection(adapter.getCount() - 1));

            // Agregar mensaje de "Pensando..." mientras espera respuesta de la IA
            chatList.add("IA: Pensando...");
            adapter.notifyDataSetChanged();
            int iaIndex = chatList.size() - 1;

            // Consultar a la IA en segundo plano
            executorService.execute(() -> {
                String respuesta = obtenerRespuestaIA(mensaje);
                mainHandler.post(() -> {
                    chatList.set(iaIndex, "IA: " + respuesta);
                    adapter.notifyDataSetChanged();
                    // Scroll al final después de recibir respuesta
                    listChat.post(() -> listChat.setSelection(adapter.getCount() - 1));
                });
            });
        });
        return view;
    }

    // Reemplaza AsyncTask por este método
    private String obtenerRespuestaIA(String mensaje) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://api.deepseek.com/v1/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("model", "deepseek-chat");
            JSONArray messages = new JSONArray();

            // Agregar mensaje del sistema para darle contexto a la IA
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Eres un asistente financiero virtual amigable y profesional. " +
                "Tu objetivo es ayudar a los usuarios con consejos sobre finanzas personales, " +
                "ahorro, inversión, gestión de gastos y metas financieras. " +
                "Responde de forma clara, concisa y siempre en español. " +
                "Sé amable y motiva a los usuarios a mejorar sus finanzas.");
            messages.put(systemMessage);

            // Agregar mensaje del usuario
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", mensaje);
            messages.put(userMessage);

            json.put("messages", messages);
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes());
            os.flush();
            os.close();
            int responseCode = conn.getResponseCode();
            BufferedReader br;
            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            if (responseCode >= 200 && responseCode < 300) {
                JSONObject response = new JSONObject(sb.toString());
                JSONArray choices = response.getJSONArray("choices");
                String respuesta = choices.getJSONObject(0).getJSONObject("message").getString("content");
                return respuesta.trim();
            } else {
                try {
                    JSONObject errorObj = new JSONObject(sb.toString());
                    if (errorObj.has("error")) {
                        JSONObject err = errorObj.getJSONObject("error");
                        return "Error IA: " + err.optString("message", "Error desconocido") + " (" + err.optString("type", "") + ")";
                    }
                } catch (Exception ex) {}
                return "Error HTTP " + responseCode + ": " + sb.toString();
            }
        } catch (Exception e) {
            return "Error al conectar con la IA: " + e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
