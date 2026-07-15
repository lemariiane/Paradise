package com.example.paradise.atividades;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.paradise.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AtividadeDiaFragment extends Fragment {

    private RecyclerView recyclerView;
    private AtividadeAdapter adapter;
    private List<AtividadeModel> lista = new ArrayList<>();
    private FirebaseFirestore db;

    private String dataRecebida;
    private String bebeId;

    public AtividadeDiaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_atividade_dia, container, false);
        recyclerView = view.findViewById(R.id.recycler_atividades);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton btnAdd = view.findViewById(R.id.btn_add);
        adapter = new AtividadeAdapter(lista, getContext(), this::abrirDialogAtividade);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            dataRecebida = getArguments().getString("data");
            bebeId = getArguments().getString("bebeId");
        }

        btnAdd.setOnClickListener(v -> abrirDialogAtividade(null));

        buscarAtividadesDoDia();

        return view;
    }

    private void buscarAtividadesDoDia() {

        LocalDate data = LocalDate.parse(dataRecebida);

        Timestamp inicioDia = new Timestamp(
                java.util.Date.from(data.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
        );

        Timestamp fimDia = new Timestamp(
                java.util.Date.from(data.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
        );

        db.collection("atividades")
                .whereEqualTo("bebeId", bebeId)
                .whereGreaterThanOrEqualTo("dataHora", inicioDia)
                .whereLessThanOrEqualTo("dataHora", fimDia)
                .orderBy("dataHora")
                .get()
                .addOnSuccessListener(query -> {

                    lista.clear();

                    for (var doc : query) {
                        AtividadeModel atividade = doc.toObject(AtividadeModel.class);
                        lista.add(atividade);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void abrirDialogAtividade(AtividadeModel atividade) {

        boolean isEdicao = (atividade != null); //true=== edição | false=== criação

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_atividade, null);

        Button btnData = dialogView.findViewById(R.id.btn_data);
        Button btnHora = dialogView.findViewById(R.id.btn_hora);
        Spinner spinnerNome = dialogView.findViewById(R.id.spinner_nome_atividade);

        List<String> atividades = Arrays.asList(
                "Banhar",
                "Mamar",
                "Brincar",
                "Ruído branco"
        );

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                atividades
        );

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNome.setAdapter(adapterSpinner);

        Calendar calendar = Calendar.getInstance();

        //EDIÇÃO
        if (isEdicao) {

            if (atividade.getDataHora() != null) {
                calendar.setTime(atividade.getDataHora().toDate());
            }

            int pos = atividades.indexOf(atividade.getNome());
            if (pos >= 0) spinnerNome.setSelection(pos);

        }
        //CRIAÇÃO
        else {

            LocalDate data = LocalDate.parse(dataRecebida);
            calendar.set(data.getYear(), data.getMonthValue() - 1, data.getDayOfMonth(), 0, 0);
        }

        btnData.setText(android.text.format.DateFormat.format("dd/MM/yyyy", calendar));
        btnHora.setText(android.text.format.DateFormat.format("HH:mm", calendar));

        btnData.setOnClickListener(v -> new DatePickerDialog(getContext(),
                (d, year, month, day) -> {
                    calendar.set(year, month, day);
                    btnData.setText(day + "/" + (month + 1) + "/" + year);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnHora.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (d, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    btnHora.setText(hour + ":" + String.format("%02d", minute));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(isEdicao ? "Editar atividade" : "Adicionar atividade")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String nome = spinnerNome.getSelectedItem().toString();
                    Timestamp dataHora = new Timestamp(calendar.getTime());

                    if (isEdicao) {
                        //UPDATE
                        atividade.setNome(nome);
                        atividade.setDataHora(dataHora);

                        FirebaseFirestore.getInstance()
                                .collection("atividades")
                                .document(atividade.getId())
                                .update(
                                        "nome", nome,
                                        "dataHora", dataHora
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Atualizado!", Toast.LENGTH_SHORT).show();
                                    buscarAtividadesDoDia();
                                });

                    } else {
                        //CREATE
                        AtividadeModel nova = new AtividadeModel();
                        nova.setBebeId(bebeId);
                        nova.setNome(nome);
                        nova.setDataHora(dataHora);

                        FirebaseFirestore.getInstance()
                                .collection("atividades")
                                .add(nova)
                                .addOnSuccessListener(doc -> {
                                    Toast.makeText(getContext(), "Salvo!", Toast.LENGTH_SHORT).show();
                                    buscarAtividadesDoDia();
                                });
                    }
                })
                .setNegativeButton("Cancelar", null);

        //só mostra excluir se for edição
        if (isEdicao) {
            builder.setNeutralButton("Excluir", (dialog, which) ->
                    FirebaseFirestore.getInstance()
                            .collection("atividades")
                            .document(atividade.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Excluído!", Toast.LENGTH_SHORT).show();
                                buscarAtividadesDoDia();
                            })
            );
        }

        builder.show();
    }
}