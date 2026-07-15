package com.example.paradise;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.paradise.bebe.BebeModel;
import com.example.paradise.bebe.BebeRepository;
import com.example.paradise.bebe.BebeSpinnerAdapter;

import java.time.LocalDate;
import java.util.List;

public abstract class Base_sono_atividade_fragment extends Fragment {

    protected Spinner spinnerBebe;
    protected CalendarView calendarView;

    protected BebeRepository bebeRepository;
    protected List<BebeModel> listaBebes;
    protected String bebeIdSelecionado;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerBebe = view.findViewById(R.id.spinner_bebe_historico);
        calendarView = view.findViewById(R.id.calendarView);

        bebeRepository = new BebeRepository();

        carregarBebes();
        configurarCalendario();
    }

    private void carregarBebes() {
        bebeRepository.buscarTodos()
                .addOnSuccessListener(bebes -> {

                    listaBebes = bebes;

                    BebeSpinnerAdapter adapter =
                            new BebeSpinnerAdapter(getContext(), bebes);

                    spinnerBebe.setAdapter(adapter);

                    spinnerBebe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            bebeIdSelecionado = listaBebes.get(position).getId();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                });
    }

    private void configurarCalendario() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {

            if (bebeIdSelecionado == null) return;

            LocalDate data = LocalDate.of(year, month + 1, dayOfMonth);

            onDiaSelecionado(bebeIdSelecionado, data);
        });
    }

    protected abstract void onDiaSelecionado(String bebeId, LocalDate data);
}