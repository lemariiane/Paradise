package com.example.paradise;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.paradise.bebe.fragment_perfil_e_bebes;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        // 1. Define qual tela abre assim que o pai loga (Inicio)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new fragment_inicio()).commit();
        }

        // 2. Configura a troca de telas pelos IDs do seu XML de Menu
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selecionado = null;

            int id = item.getItemId();

            if (id == R.id.inicio) {
                selecionado = new fragment_inicio();
            } else if (id == R.id.perfil_e_bebe) {
                selecionado = new fragment_perfil_e_bebes();
            } else if (id == R.id.atividades) {
                // selecionado = new fragment_atividades();
            } else if (id == R.id.insights) {
                // selecionado = new fragment_insights();
            } else if (id == R.id.nao_sei) {
                // selecionado = new fragment_perfil();
            }

            // Se a tela existir, faz a troca
            if (selecionado != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, selecionado)
                        .commit();
            }
            return true;
        });
    }

}