package com.example.paradise;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.paradise.bebe.BebeModel;
import com.example.paradise.bebe.BebeRepository;
import com.example.paradise.bebe.BebeSpinnerAdapter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InsightsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InsightsFragment extends Fragment {

    private Spinner spinnerBebe;

    private BebeRepository bebeRepository;
    private List<BebeModel> listaBebes;
    private String bebeIdSelecionado;

    private LineChart chartQualidadeSono;

    private BarChart chartPeriodosDia;
    private TextView txtMelhorPeriodo;

    private TextView txtAtividades, txtHorarios, txtQualidadeSono;

    public static InsightsFragment newInstance(String param1, String param2) {
        InsightsFragment fragment = new InsightsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public InsightsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        escutarNovosInsights("22a54c39-302d-4365-9ee7-87c676b8eb37");
        if (getArguments() != null) {

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pedirPermissaoDeNotificacao();

        spinnerBebe = view.findViewById(R.id.spinner_bebe_historico);

        chartQualidadeSono = view.findViewById(R.id.chartQualidadeSono);
        txtAtividades = view.findViewById(R.id.txtAtividades);
        txtHorarios = view.findViewById(R.id.txtHorarios);
        txtQualidadeSono = view.findViewById(R.id.txtQualidadeSono);
        chartPeriodosDia = view.findViewById(R.id.chartPeriodosDia);
        txtMelhorPeriodo  = view.findViewById(R.id.txtMelhorPeriodo);

        bebeRepository = new BebeRepository();

        carregarBebes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insights, container, false);
    }

    private void pedirPermissaoDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void carregarBebes() {
        bebeRepository.buscarTodos()
                .addOnSuccessListener(bebes -> {

                    listaBebes = bebes;

                    BebeSpinnerAdapter adapter =
                            new BebeSpinnerAdapter(requireContext(), bebes);

                    spinnerBebe.setAdapter(adapter);

                    configurarSpinner();
                });
    }

    private void configurarSpinner() {
        spinnerBebe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bebeIdSelecionado = listaBebes.get(position).getId();
                carregarInsights(bebeIdSelecionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void carregarInsights(String bebeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Log.d("INSIGHTS", "Buscando insights para: " + bebeId);

        carregarGraficoQualidade(bebeId);


        db.collection("insights")
                .document(bebeId)
                .collection("periodo_sono_longo")
                .orderBy("semana", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String periodo = doc.getString("periodo");
                        String turno = doc.getString("turno");

                    } else {
                        txtHorarios.setText("Não possui dados o suficiente...");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("INSIGHTS", "Erro periodo_sono_longo: " + e.getMessage());
                    txtHorarios.setText("Erro ao carregar");
                });

        db.collection("insights")
                .document(bebeId)
                .collection("periodo_sono_longo")
                .orderBy("semana", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        chartPeriodosDia.setNoDataText("Sem dados suficientes");
                        return;
                    }

                    List<Map<String, Object>> registros = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Double duracao = doc.getDouble("duracao_media");
                        String periodo = doc.getString("periodo");
                        String turno   = doc.getString("turno");

                        if (duracao != null && periodo != null) {
                            Map<String, Object> r = new HashMap<>();
                            r.put("periodo",      periodo);
                            r.put("turno",        turno);
                            r.put("duracao_media", duracao);
                            registros.add(r);
                        }
                    }

                    if (!registros.isEmpty()) {
                        carregarGraficoPeriodos(registros);
                    } else {
                        chartPeriodosDia.setNoDataText("Sem dados suficientes");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("INSIGHTS", "Erro grafico_periodos: " + e.getMessage());
                    chartPeriodosDia.setNoDataText("Erro ao carregar");
                });


        db.collection("insights")
                .document(bebeId)
                .collection("atividades_sonos_longos")
                .orderBy("semana", Query.Direction.DESCENDING)
                .orderBy("total_sonos_longos_precedidos", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            String atividade = doc.getString("atividade");
                            long total = doc.getLong("total_sonos_longos_precedidos") != null
                                    ? doc.getLong("total_sonos_longos_precedidos") : 0;
                            sb.append("• ").append(atividade)
                                    .append(" (").append(total).append("x)\n");
                        }
                        txtAtividades.setText(sb.toString().trim());
                    } else {
                        txtAtividades.setText("Não possui dados o suficiente...");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("INSIGHTS", "Erro atividades_sonos_longos: " + e.getMessage());
                    txtAtividades.setText("Erro ao carregar");
                });
    }

    private void carregarGraficoQualidade(String bebeId) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("insights")
                .document(bebeId)
                .collection("qualidade_sono")
                .orderBy("semana", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> semanas = new ArrayList<>();

                    chartQualidadeSono.getAxisLeft().setDrawGridLines(false);
                    chartQualidadeSono.getXAxis().setDrawGridLines(false);

                    int index = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        String qualidade =
                                doc.getString("qualidade_sono");

                        float score =
                                converterQualidadeParaScore(qualidade);

                        entries.add(new Entry(index, score));

                        Timestamp timestamp =
                                doc.getTimestamp("semana");

                        if (timestamp != null) {

                            SimpleDateFormat sdf =
                                    new SimpleDateFormat("dd/MM");

                            semanas.add(
                                    sdf.format(timestamp.toDate())
                            );

                        } else {
                            semanas.add("");
                        }

                        index++;
                    }

                    ArrayList<Float> scores = new ArrayList<>();
                    for (Entry e : entries) scores.add(e.getY());
                    txtQualidadeSono.setText(gerarInsightQualidade(scores));

                    LineDataSet dataSet =
                            new LineDataSet(entries, "");

                    // Linha
                    dataSet.setLineWidth(4f);

                    // Pontos
                    dataSet.setCircleRadius(7f);

                    dataSet.setDrawValues(false);

                    dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

                    // Cor da linha
                    dataSet.setColor(
                            getResources().getColor(R.color.roxo)
                    );

                    // Cor dos pontos
                    dataSet.setCircleColor(
                            getResources().getColor(R.color.roxo_claro)
                    );

                    dataSet.setDrawCircleHole(false);

                    LineData lineData =
                            new LineData(dataSet);

                    chartQualidadeSono.setData(lineData);

                    chartQualidadeSono.getDescription()
                            .setEnabled(false);

                    chartQualidadeSono.getLegend()
                            .setEnabled(false);

                    // Remove eixo direito
                    chartQualidadeSono.getAxisRight()
                            .setEnabled(false);

                    // EIXO X
                    chartQualidadeSono.getXAxis()
                            .setPosition(XAxis.XAxisPosition.BOTTOM);

                    chartQualidadeSono.getXAxis()
                            .setGranularity(1f);

                    chartQualidadeSono.getXAxis()
                            .setLabelCount(4, true);

                    chartQualidadeSono.getXAxis()
                            .setValueFormatter(
                                    new IndexAxisValueFormatter(semanas)
                            );

                    // EIXO Y
                    chartQualidadeSono.getAxisLeft()
                            .setAxisMinimum(1f);

                    chartQualidadeSono.getAxisLeft()
                            .setAxisMaximum(3f);

                    chartQualidadeSono.getAxisLeft()
                            .setGranularity(0.5f);

                    chartQualidadeSono.getAxisLeft()
                            .setValueFormatter(new ValueFormatter() {

                                @Override
                                public String getFormattedValue(float value) {

                                    if (value == 1f)
                                        return "Ruim";

                                    if (value == 1.5f)
                                        return "Irregular";

                                    if (value == 2f)
                                        return "Regular";

                                    if (value == 3f)
                                        return "Bom";

                                    return "";
                                }
                            });

                    chartQualidadeSono.animateX(1200);

                    chartQualidadeSono.invalidate();
                });
    }

    private float converterQualidadeParaScore(String qualidade) {

        if (qualidade == null) return 1f;

        switch (qualidade.toLowerCase()) {

            case "ruim":
                return 1f;

            case "irregular":
                return 1.5f;

            case "regular":
                return 2f;

            case "bom":
                return 3f;

            default:
                return 1f;
        }
    }

    private String gerarInsightQualidade(ArrayList<Float> scores) {

        if (scores.size() < 2) {

            return "Ainda não há dados suficientes para acompanhar a evolução do sono.";
        }

        float semanaAnterior =
                scores.get(scores.size() - 2);

        float semanaAtual =
                scores.get(scores.size() - 1);

        // melhora
        if (semanaAtual > semanaAnterior) {

            return "A qualidade do sono melhorou em relação à semana anterior. "
                    + "Manter uma rotina consistente pode ajudar a preservar esse progresso.";
        }

        // piora
        if (semanaAtual < semanaAnterior) {

            return "A qualidade do sono piorou em relação à semana anterior. "
                    + "Que tal tentar horários mais regulares e um ambiente mais calmo antes de dormir?";
        }

        // sono bom
        if (semanaAtual >= 4f) {

            return "O bebê manteve um padrão de sono considerado bom. "
                    + "Rotinas previsíveis e momentos tranquilos antes do sono ajudam nessa estabilidade.";
        }

        // sono regular
        if (semanaAtual >= 3f) {

            return "O padrão de sono permaneceu estável nesta semana. "
                    + "Manter horários consistentes ajuda o bebê a desenvolver hábitos de sono saudáveis.";
        }

        // irregular
        return "O bebê continua apresentando um padrão de sono irregular. "
                + "Atividades relaxantes e horários consistentes podem ajudar na adaptação do sono.";
    }



    private void carregarGraficoPeriodos(List<Map<String, Object>> registros) {
        Map<String, Double> somaPorPeriodo  = new LinkedHashMap<>();
        Map<String, Integer> contPorPeriodo = new LinkedHashMap<>();

        for (Map<String, Object> r : registros) {
            String periodo = (String) r.get("periodo");
            double duracao = ((Number) r.get("duracao_media")).doubleValue();

            somaPorPeriodo.put(periodo,  somaPorPeriodo.getOrDefault(periodo,  0.0) + duracao);
            contPorPeriodo.put(periodo, contPorPeriodo.getOrDefault(periodo, 0)   + 1);
        }

        List<String>  periodos = new ArrayList<>(somaPorPeriodo.keySet());
        Collections.sort(periodos);

        List<Float> medias = new ArrayList<>();
        for (String p : periodos) {
            double media = somaPorPeriodo.get(p) / contPorPeriodo.get(p);
            medias.add((float) media);
        }

        configurarBarChart(periodos, medias);
    }


    /**
     * Monta e estiliza o BarChart
     */
    private void configurarBarChart(List<String> periodos, List<Float> medias) {

        chartPeriodosDia.clear();
        chartPeriodosDia.invalidate();


        List<BarEntry> entries = new ArrayList<>();
        int melhorIdx = 0;
        float melhorValor = -1f;

        for (int i = 0; i < medias.size(); i++) {
            entries.add(new BarEntry(i, medias.get(i)));
            if (medias.get(i) > melhorValor) {
                melhorValor = medias.get(i);
                melhorIdx   = i;
            }
        }


        int corDestaque = Color.parseColor("#0D0561");
        int corNormal   = Color.parseColor("#c6d4e1");

        int[] cores = new int[entries.size()];
        for (int i = 0; i < cores.length; i++) {
            cores[i] = (i == melhorIdx) ? corDestaque : corNormal;
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(cores);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.parseColor("#555555"));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatarDuracao(value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.55f);


        chartPeriodosDia.getXAxis().setValueFormatter(new IndexAxisValueFormatter(periodos));
        chartPeriodosDia.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartPeriodosDia.getXAxis().setGranularity(1f);
        chartPeriodosDia.getXAxis().setTextSize(10f);
        chartPeriodosDia.getXAxis().setTextColor(Color.parseColor("#777777"));
        chartPeriodosDia.getXAxis().setDrawGridLines(false);
        chartPeriodosDia.getXAxis().setDrawAxisLine(false);
        chartPeriodosDia.getXAxis().setLabelRotationAngle(-35f);


        chartPeriodosDia.getAxisLeft().setEnabled(false);
        chartPeriodosDia.getAxisRight().setEnabled(false);


        chartPeriodosDia.setData(barData);
        chartPeriodosDia.getDescription().setEnabled(false);
        chartPeriodosDia.getLegend().setEnabled(false);
        chartPeriodosDia.setDrawGridBackground(false);
        chartPeriodosDia.setDrawBorders(false);
        chartPeriodosDia.setTouchEnabled(false);
        chartPeriodosDia.setFitBars(true);
        chartPeriodosDia.animateY(600, Easing.EaseInOutQuad);
        chartPeriodosDia.invalidate();


        String melhorPeriodoLabel = periodos.get(melhorIdx);
        txtMelhorPeriodo.setText(
                "O bebê dorme por mais tempo no período " + melhorPeriodoLabel +
                        " (" + formatarDuracao(melhorValor) + " em média)."
        );
    }



    private String formatarDuracao(float minutos) {
        int h   = (int) (minutos / 60);
        int min = Math.round(minutos % 60);
        if (h == 0) return min + "min";
        if (min == 0) return h + "h";
        return h + "h " + min + "min";
    }

    private boolean primeiraLeitura = true;

    private void escutarNovosInsights(String bebeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("insights")
                .document(bebeId)
                .collection("qualidade_sono")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("ParadiseApp", "Erro ao escutar insights: ", error);
                            return;
                        }

                        if (primeiraLeitura) {
                            primeiraLeitura = false;
                            return;
                        }

                        if (value != null && !value.getMetadata().hasPendingWrites()) {

                            gerarNotificacaoLocal("✨ Paradise: Novos Insights!",
                                    "A análise de sono da última semana já está pronta. Venha conferir!");
                        }
                    }
                });
    }

    private void gerarNotificacaoLocal(String titulo, String mensagem) {
        String channelId = "canal_insights_paradise";

        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notificações de Insights",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Avisa quando novos relatórios estatísticos de sono ficam prontos.");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(requireContext(), requireActivity().getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(101, builder.build());
        }
    }
}