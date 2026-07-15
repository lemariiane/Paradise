package com.example.paradise.sono;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentTransaction;

import com.example.paradise.Base_sono_atividade_fragment;
import com.example.paradise.R;

import java.time.LocalDate;

public class fragment_sono extends Base_sono_atividade_fragment {

    public fragment_sono() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_sono, container, false);
    }

    @Override
    protected void onDiaSelecionado(String bebeId, LocalDate data) {

        Sono_dia_fragment fragment = new Sono_dia_fragment();

        Bundle bundle = new Bundle();
        bundle.putString("data", data.toString());
        bundle.putString("bebeId", bebeId);

        fragment.setArguments(bundle);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}