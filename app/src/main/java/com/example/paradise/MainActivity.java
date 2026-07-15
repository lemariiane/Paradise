package com.example.paradise;

import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.paradise.bebe.fragment_perfil_e_bebes;
import com.example.paradise.sono.fragment_sono;
import com.example.paradise.atividades.AtividadeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        configurarVideoBg();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, new fragment_inicio()).commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selecionado = null;

            int id = item.getItemId();

            if (id == R.id.inicio) {
                selecionado = new fragment_inicio();
            } else if (id == R.id.sono) {
                selecionado = new fragment_sono();
            } else if (id == R.id.perfil_e_bebe) {
                selecionado = new fragment_perfil_e_bebes();
            } else if (id == R.id.atividades) {
                selecionado = new AtividadeFragment();
            } else if (id == R.id.insights) {
                selecionado = new InsightsFragment();
            }

            if (selecionado != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, selecionado)
                        .commit();
            }
            return true;
        });
    }

    private void configurarVideoBg() {
        VideoView videoBg = findViewById(R.id.video_bg);

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bg_estrelas);
        videoBg.setVideoURI(uri);

        videoBg.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);

            float videoWidth = mp.getVideoWidth();
            float videoHeight = mp.getVideoHeight();
            float viewWidth = videoBg.getWidth();
            float viewHeight = videoBg.getHeight();

            float xScale = viewWidth / videoWidth;
            float yScale = viewHeight / videoHeight;
            float scale = Math.max(xScale, yScale);

            videoBg.setScaleX(scale);
            videoBg.setScaleY(scale);
            // -------------------------------------------------------

            videoBg.start();
        });

        videoBg.setOnCompletionListener(mp -> videoBg.start());
    }

}