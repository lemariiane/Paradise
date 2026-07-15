package com.example.paradise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.paradise.bebe.BebeModel;
import com.example.paradise.bebe.BebeRepository;
import com.example.paradise.bebe.BebeSpinnerAdapter;
import com.example.paradise.sono.JanelaSono;
import com.example.paradise.sono.SonoModel;
import com.example.paradise.sono.SonoRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class fragment_inicio extends Fragment {

    private static final String TAG = "fragment_inicio";

    private static final String PREF_SONO_ID = "sono_em_andamento_id";
    private static final String PREF_BEBE_ID = "ultimo_bebe_id";


    private ImageButton btnSono, btnAcordar;
    private TextView txtStatus, txtUltimaSoneca, txtJanelaSono;;
    private Spinner spinnerBebe;


    private FirebaseAuth auth;
    private BebeRepository bebeRepository;
    private SonoRepository sonoRepository;
    private SharedPreferences prefs;


    private List<BebeModel> listaBebes;
    private BebeModel bebeSelecionado;
    private SonoModel sonoEmAndamento;


    private BebeSpinnerAdapter bebeAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        initViews(view);

        initRepositories();

        prefs = requireActivity().getSharedPreferences("paradise_prefs", getContext().MODE_PRIVATE);

        verificarLogin();

        configurarSpinner();
        configurarBotoes();
    }

    private void initViews(View view) {
        btnSono = view.findViewById(R.id.btn_sono);
        btnAcordar = view.findViewById(R.id.btn_acordar);
        txtStatus = view.findViewById(R.id.txt_status);
        txtUltimaSoneca = view.findViewById(R.id.txt_ultima_soneca);
        txtJanelaSono = view.findViewById(R.id.txt_janela_sono);
        spinnerBebe = view.findViewById(R.id.spinner_bebe);
    }

    private void initRepositories() {
        auth = FirebaseAuth.getInstance();
        bebeRepository = new BebeRepository();
        sonoRepository = new SonoRepository();
    }

    private void verificarLogin() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Usuário não logado", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            return;
        }

        carregarBebes();
    }

    private void carregarBebes() {
        bebeRepository.buscarTodos()
                .addOnSuccessListener(bebes -> {
                    listaBebes = bebes;

                    if (bebes.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Cadastre um bebê primeiro",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Configura adapter do spinner
                    bebeAdapter = new BebeSpinnerAdapter(getContext(), bebes);
                    spinnerBebe.setAdapter(bebeAdapter);

                    // Tenta restaurar último bebê selecionado
                    String ultimoBebeId = prefs.getString(PREF_BEBE_ID, null);
                    if (ultimoBebeId != null) {
                        for (int i = 0; i < bebes.size(); i++) {
                            if (bebes.get(i).getId().equals(ultimoBebeId)) {
                                spinnerBebe.setSelection(i);
                                break;
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao carregar bebês: " + e.getMessage());
                    Toast.makeText(getContext(),
                            "Erro ao carregar bebês: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void configurarSpinner() {
        spinnerBebe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listaBebes != null && !listaBebes.isEmpty()) {
                    // Bebê selecionado mudou
                    bebeSelecionado = listaBebes.get(position);

                    Log.d(TAG, "Bebê selecionado: " + bebeSelecionado.getNome());

                    // Salva preferência
                    prefs.edit().putString(PREF_BEBE_ID, bebeSelecionado.getId()).apply();

                    // Verifica se este bebê tem sono em andamento
                    verificarSonoEmAndamento(bebeSelecionado.getId());

                    // Carrega última soneca deste bebê
                    carregarUltimaSoneca(bebeSelecionado.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                bebeSelecionado = null;
            }
        });
    }

    private void verificarSonoEmAndamento(String bebeId) {
        sonoRepository.buscarSonoEmAndamento(bebeId)
                .addOnSuccessListener(sono -> {
                    sonoEmAndamento = sono;

                    if (sono != null) {
                        // Tem um sono em andamento
                        Log.d(TAG, "Sono em andamento encontrado: " + sono.getId());

                        // Salva ID localmente
                        prefs.edit().putString(PREF_SONO_ID, sono.getId()).apply();

                        // Atualiza UI para modo "dormindo"
                        atualizarUIModoDormindo(true);
                    } else {
                        // Não tem sono em andamento
                        Log.d(TAG, "Nenhum sono em andamento");
                        prefs.edit().remove(PREF_SONO_ID).apply();
                        atualizarUIModoDormindo(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar sono: " + e.getMessage());
                    atualizarUIModoDormindo(false);
                });
    }

    private void carregarUltimaSoneca(String bebeId) {
        sonoRepository.buscarUltimasSonecas(bebeId, 1)
                .addOnSuccessListener(sonos -> {
                    if (!sonos.isEmpty()) {
                        SonoModel ultima = sonos.get(0);
                        String texto = "Última soneca durou " +
                                formatarDuracao(ultima.getDuracaoMinutos());
                        txtUltimaSoneca.setText(texto);
                    } else {
                        txtUltimaSoneca.setText("Última soneca durou --");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar última soneca: " + e.getMessage());
                    txtUltimaSoneca.setText("Última soneca durou --");
                });
    }

    private void configurarBotoes() {
        // BOTÃO LUA - Bebê dormiu
        btnSono.setOnClickListener(v -> {
            if (bebeSelecionado == null) {
                Toast.makeText(getContext(),
                        "Selecione um bebê primeiro",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Desabilita botão para evitar duplo clique
            btnSono.setEnabled(false);

            sonoRepository.iniciarSono(bebeSelecionado.getId())
                    .addOnSuccessListener(documentReference -> {
                        sonoRepository.buscarPorId(documentReference.getId())
                                .addOnSuccessListener(sono -> {
                                    sonoEmAndamento = sono;

                                    // Salva ID localmente
                                    prefs.edit()
                                            .putString(PREF_SONO_ID, sono.getId())
                                            .apply();

                                    atualizarUIModoDormindo(true);
                                    btnSono.setEnabled(true);

                                    String mensagem = bebeSelecionado.getNome() +
                                            " dormiu! 💤";
                                    Toast.makeText(getContext(), mensagem, Toast.LENGTH_SHORT).show();

                                    Log.d(TAG, "Sono iniciado com ID: " + sono.getId());
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnSono.setEnabled(true);
                        Log.e(TAG, "Erro ao iniciar sono: " + e.getMessage());
                        Toast.makeText(getContext(),
                                "Erro: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });

        // BOTÃO SOL - Bebê acordou
        btnAcordar.setOnClickListener(v -> {
            if (sonoEmAndamento == null) {
                Toast.makeText(getContext(),
                        "Nenhum sono em andamento",
                        Toast.LENGTH_SHORT).show();
                atualizarUIModoDormindo(false);
                return;
            }

            btnAcordar.setEnabled(false);

            sonoRepository.finalizarSono(sonoEmAndamento.getId())
                    .addOnSuccessListener(aVoid -> {
                        sonoRepository.buscarPorId(sonoEmAndamento.getId())
                                .addOnSuccessListener(sonoFinalizado -> {
                                    sonoEmAndamento = null;

                                    prefs.edit().remove(PREF_SONO_ID).apply();

                                    atualizarUIModoDormindo(false);

                                    String texto = "Última soneca durou " +
                                            formatarDuracao(sonoFinalizado.getDuracaoMinutos());
                                    txtUltimaSoneca.setText(texto);

                                    btnAcordar.setEnabled(true);

                                    String mensagem = bebeSelecionado.getNome() +
                                            " acordou! ☀️ Duração: " +
                                            formatarDuracao(sonoFinalizado.getDuracaoMinutos());
                                    Toast.makeText(getContext(), mensagem, Toast.LENGTH_LONG).show();

                                    Log.d(TAG, "Sono finalizado. Duração: " +
                                            sonoFinalizado.getDuracaoMinutos() + " minutos");
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnAcordar.setEnabled(true);
                        Log.e(TAG, "Erro ao finalizar sono: " + e.getMessage());
                        Toast.makeText(getContext(),
                                "Erro: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }

    private void atualizarUIModoDormindo(boolean dormindo) {
        if (dormindo && sonoEmAndamento != null) {
            btnSono.setVisibility(View.INVISIBLE);
            btnAcordar.setVisibility(View.VISIBLE);

            // Formata hora do início
            Date dataInicio = sonoEmAndamento.getInicio().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String hora = sdf.format(dataInicio);

            String nomeBebe = bebeSelecionado != null ?
                    bebeSelecionado.getNome() : "Bebê";
            txtStatus.setText(nomeBebe + " dormindo desde " + hora);

        } else {
            // Bebê está acordado - mostra botão lua
            btnAcordar.setVisibility(View.INVISIBLE);
            btnSono.setVisibility(View.VISIBLE);

            String nomeBebe = bebeSelecionado != null ?
                    bebeSelecionado.getNome() : "O bebê";
            txtStatus.setText(nomeBebe + " está acordado");
        }

        if (bebeSelecionado != null) {
            calcularEExibirJanela(bebeSelecionado.getId(), bebeSelecionado.getIdadeEmSemanas());
        }
    }

    private String formatarDuracao(Long minutos) {
        if (minutos == null) return "--";

        if (minutos < 60) {
            return minutos + " min";
        }

        long horas = minutos / 60;
        long minsRestantes = minutos % 60;

        if (minsRestantes == 0) {
            return horas + "h";
        } else {
            return String.format(Locale.getDefault(), "%dh%02dmin",
                    horas, minsRestantes);
        }
    }

    private void calcularEExibirJanela(String bebeId, int idadeSemanas) {
        sonoRepository.calcularJanelaDeVigilia(bebeId, idadeSemanas)
                .addOnSuccessListener(janelas -> {

                    if (janelas == null || janelas.length == 0) {
                        txtJanelaSono.setText("Registre sonecas/despertares para visualizar a janela de vigília");
                        return;
                    }

                    JanelaSono janela = janelas[0];
                    long agoraMs = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    long inicioMs = janela.getMinMin();
                    long fimMs    = janela.getMaxMin();

                    if (agoraMs >= fimMs) {
                        // Passou do limite máximo
                        txtJanelaSono.setText("Sinais de cansaço podem aparecer agora 😴");

                    } else if (agoraMs >= inicioMs) {
                        // Está dentro da janela
                        String horaFim = sdf.format(new Date(fimMs));
                        txtJanelaSono.setText("Janela de sono aberta 🌙 até ~" + horaFim);

                    } else {
                        // Janela ainda não começou
                        String horaInicio = sdf.format(new Date(inicioMs));
                        String horaFim    = sdf.format(new Date(fimMs));
                        long faltaMin     = (inicioMs - agoraMs) / (60 * 1000);

                        if (faltaMin < 60) {
                            txtJanelaSono.setText("Próxima soneca em " + faltaMin +
                                    "min (" + horaInicio + " ~ " + horaFim + ")");
                        } else {
                            long h = faltaMin / 60;
                            long m = faltaMin % 60;
                            txtJanelaSono.setText(String.format(Locale.getDefault(),
                                    "Próxima soneca em %dh%02dmin (%s ~ %s)",
                                    h, m, horaInicio, horaFim));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    txtJanelaSono.setText("Próxima soneca: --");
                    Log.e(TAG, "Erro ao calcular janela: " + e.getMessage());
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (auth.getCurrentUser() != null && listaBebes != null && bebeSelecionado != null) {
            verificarSonoEmAndamento(bebeSelecionado.getId());
            carregarUltimaSoneca(bebeSelecionado.getId());
        }
    }
}