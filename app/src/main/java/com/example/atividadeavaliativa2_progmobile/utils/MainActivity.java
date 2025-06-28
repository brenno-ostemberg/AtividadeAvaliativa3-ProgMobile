package com.example.atividadeavaliativa2_progmobile.utils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.ui.activity.ListaUsuariosActivity;
import com.example.atividadeavaliativa2_progmobile.ui.activity.ListaPartidasActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button botaoControleJogadores = findViewById(R.id.botaoControleJogadores);
        botaoControleJogadores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListaUsuariosActivity.class);
                startActivity(intent);
            }
        });

        Button botaoControlePartidas = findViewById(R.id.botaoControlePartidas);
        botaoControlePartidas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListaPartidasActivity.class);
                startActivity(intent);
            }
        });

    }
}