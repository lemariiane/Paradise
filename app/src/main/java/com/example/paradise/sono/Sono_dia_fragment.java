package com.example.paradise.sono;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.paradise.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Sono_dia_fragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton btnAdd;

    private SonoRepository sonoRepository;
    private SonoAdapter adapter;

    private String bebeIdRecebido;
    private String dataRecebida;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sono_dia, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            bebeIdRecebido = getArguments().getString("bebeId");
            dataRecebida = getArguments().getString("data");
        }

        initViews(view);
        sonoRepository = new SonoRepository();

        if (bebeIdRecebido != null && dataRecebida != null) {
            carregarSonosDoDia();
        }
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_sonos);
        btnAdd = view.findViewById(R.id.btn_add_sono);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SonoAdapter(new ArrayList<>(), getContext(), this::abrirDialogEditarSono);
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            if (bebeIdRecebido == null) {
                Toast.makeText(getContext(), "Bebê não identificado", Toast.LENGTH_SHORT).show();
                return;
            }
            abrirDialogAdicionarSono();
        });
    }

    private void carregarSonosDoDia() {
        LocalDate data = LocalDate.parse(dataRecebida);

        Timestamp inicioDia = new Timestamp(
                java.util.Date.from(data.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
        );
        Timestamp fimDia = new Timestamp(
                java.util.Date.from(data.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
        );

        sonoRepository.buscarSonecasPorPeriodo(bebeIdRecebido, inicioDia, fimDia)
                .addOnSuccessListener(sonos -> adapter.atualizarLista(sonos))
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao carregar", Toast.LENGTH_SHORT).show());
    }

    private void abrirDialogAdicionarSono() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_sono, null);

        Button btnDataInicio = dialogView.findViewById(R.id.btn_data_inicio);
        Button btnHoraInicio = dialogView.findViewById(R.id.btn_hora_inicio);
        Button btnDataFim = dialogView.findViewById(R.id.btn_data_fim);
        Button btnHoraFim = dialogView.findViewById(R.id.btn_hora_fim);

        // 🔥 Pré-preenche com a data do dia selecionado no calendário
        LocalDate dataDia = LocalDate.parse(dataRecebida);

        Calendar inicio = Calendar.getInstance();
        inicio.set(dataDia.getYear(), dataDia.getMonthValue() - 1, dataDia.getDayOfMonth(), 0, 0, 0);

        Calendar fim = Calendar.getInstance();
        fim.set(dataDia.getYear(), dataDia.getMonthValue() - 1, dataDia.getDayOfMonth(), 0, 0, 0);

        // 🔥 Mostra a data já preenchida nos botões
        btnDataInicio.setText(android.text.format.DateFormat.format("dd/MM/yyyy", inicio));
        btnHoraInicio.setText("00:00");
        btnDataFim.setText(android.text.format.DateFormat.format("dd/MM/yyyy", fim));
        btnHoraFim.setText("00:00");

        btnDataInicio.setOnClickListener(v -> new DatePickerDialog(getContext(),
                (d, year, month, day) -> {
                    inicio.set(year, month, day);
                    btnDataInicio.setText(day + "/" + (month + 1) + "/" + year);
                },
                inicio.get(Calendar.YEAR), inicio.get(Calendar.MONTH), inicio.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnHoraInicio.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (d, hour, minute) -> {
                    inicio.set(Calendar.HOUR_OF_DAY, hour);
                    inicio.set(Calendar.MINUTE, minute);
                    btnHoraInicio.setText(hour + ":" + String.format("%02d", minute));
                },
                inicio.get(Calendar.HOUR_OF_DAY), inicio.get(Calendar.MINUTE), true
        ).show());

        // 🔥 Data do fim é editável — permite dia seguinte
        btnDataFim.setOnClickListener(v -> new DatePickerDialog(getContext(),
                (d, year, month, day) -> {
                    fim.set(year, month, day);
                    btnDataFim.setText(day + "/" + (month + 1) + "/" + year);
                },
                fim.get(Calendar.YEAR), fim.get(Calendar.MONTH), fim.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnHoraFim.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (d, hour, minute) -> {
                    fim.set(Calendar.HOUR_OF_DAY, hour);
                    fim.set(Calendar.MINUTE, minute);
                    btnHoraFim.setText(hour + ":" + String.format("%02d", minute));
                },
                fim.get(Calendar.HOUR_OF_DAY), fim.get(Calendar.MINUTE), true
        ).show());

        new AlertDialog.Builder(getContext())
                .setTitle("Adicionar sono")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    Date dataInicio = inicio.getTime();
                    Date dataFim = fim.getTime();

                    if (!dataInicio.before(dataFim)) {
                        Toast.makeText(getContext(), "Início deve ser antes do fim", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    salvarSono(dataInicio, dataFim);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private void salvarSono(Date inicioDate, Date fimDate) {
        Timestamp inicio = new Timestamp(inicioDate);
        Timestamp fim = new Timestamp(fimDate);

        if (temConflito(inicio, fim, adapter.getLista())) {
            Toast.makeText(getContext(), "Já existe um sono nesse horário", Toast.LENGTH_LONG).show();
            return;
        }

        SonoModel sono = new SonoModel();
        sono.setBebeId(bebeIdRecebido);
        sono.setInicio(inicio);
        sono.setFim(fim);
        sono.setStatus("FINALIZADO");
        sono.setCriadoEm(Timestamp.now());
        sono.setAtualizadoEm(Timestamp.now());
        sono.setDuracaoMinutos((fim.getSeconds() - inicio.getSeconds()) / 60);

        sonoRepository.salvarSonoManual(sono)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Sono salvo!", Toast.LENGTH_SHORT).show();
                    carregarSonosDoDia();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao salvar", Toast.LENGTH_SHORT).show());
    }

    private void abrirDialogEditarSono(SonoModel sono) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_sono, null);

        Button btnDataInicio = dialogView.findViewById(R.id.btn_data_inicio);
        Button btnHoraInicio = dialogView.findViewById(R.id.btn_hora_inicio);
        Button btnDataFim = dialogView.findViewById(R.id.btn_data_fim);
        Button btnHoraFim = dialogView.findViewById(R.id.btn_hora_fim);

        Calendar inicio = Calendar.getInstance();
        Calendar fim = Calendar.getInstance();

        if (sono.getInicio() != null) inicio.setTime(sono.getInicio().toDate());
        if (sono.getFim() != null) fim.setTime(sono.getFim().toDate());

        btnDataInicio.setText(android.text.format.DateFormat.format("dd/MM/yyyy", inicio));
        btnHoraInicio.setText(android.text.format.DateFormat.format("HH:mm", inicio));
        btnDataFim.setText(android.text.format.DateFormat.format("dd/MM/yyyy", fim));
        btnHoraFim.setText(android.text.format.DateFormat.format("HH:mm", fim));

        btnDataInicio.setOnClickListener(v -> new DatePickerDialog(getContext(),
                (d, year, month, day) -> {
                    inicio.set(year, month, day);
                    btnDataInicio.setText(day + "/" + (month + 1) + "/" + year);
                },
                inicio.get(Calendar.YEAR), inicio.get(Calendar.MONTH), inicio.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnHoraInicio.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (d, hour, minute) -> {
                    inicio.set(Calendar.HOUR_OF_DAY, hour);
                    inicio.set(Calendar.MINUTE, minute);
                    btnHoraInicio.setText(hour + ":" + String.format("%02d", minute));
                },
                inicio.get(Calendar.HOUR_OF_DAY), inicio.get(Calendar.MINUTE), true
        ).show());

        btnDataFim.setOnClickListener(v -> new DatePickerDialog(getContext(),
                (d, year, month, day) -> {
                    fim.set(year, month, day);
                    btnDataFim.setText(day + "/" + (month + 1) + "/" + year);
                },
                fim.get(Calendar.YEAR), fim.get(Calendar.MONTH), fim.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnHoraFim.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (d, hour, minute) -> {
                    fim.set(Calendar.HOUR_OF_DAY, hour);
                    fim.set(Calendar.MINUTE, minute);
                    btnHoraFim.setText(hour + ":" + String.format("%02d", minute));
                },
                fim.get(Calendar.HOUR_OF_DAY), fim.get(Calendar.MINUTE), true
        ).show());

        new AlertDialog.Builder(getContext())
                .setTitle("Editar sono")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    Timestamp novoInicio = new Timestamp(inicio.getTime());
                    Timestamp novoFim = new Timestamp(fim.getTime());

                    sono.setInicio(novoInicio);
                    sono.setFim(novoFim);
                    sono.setAtualizadoEm(Timestamp.now());
                    sono.setDuracaoMinutos((novoFim.getSeconds() - novoInicio.getSeconds()) / 60);

                    sonoRepository.atualizarSono(sono)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Sono atualizado!", Toast.LENGTH_SHORT).show();
                                carregarSonosDoDia();
                            });
                })
                .setNeutralButton("Excluir", (dialog, which) ->
                        sonoRepository.deletarSono(sono.getId())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Sono excluído!", Toast.LENGTH_SHORT).show();
                                    carregarSonosDoDia();
                                })
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean temConflito(Timestamp novoInicio, Timestamp novoFim, List<SonoModel> lista) {
        for (SonoModel sono : lista) {
            if (!"FINALIZADO".equals(sono.getStatus())) continue;
            Timestamp inicio = sono.getInicio();
            Timestamp fim = sono.getFim();
            if (inicio == null || fim == null) continue;
            if (novoInicio.toDate().before(fim.toDate()) && novoFim.toDate().after(inicio.toDate())) {
                return true;
            }
        }
        return false;
    }
}