package com.example.paradise.atividades;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.paradise.Base_sono_atividade_fragment;
import com.example.paradise.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class AtividadeFragment extends Base_sono_atividade_fragment {

    private FirebaseFirestore db;

    public AtividadeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_atividade, container, false);

        View cardBanho = view.findViewById(R.id.card_banho);
        View cardMamar = view.findViewById(R.id.card_mamar);
        View cardBrincar = view.findViewById(R.id.card_brincar);
        View cardRuido = view.findViewById(R.id.card_ruido);

        cardBanho.setOnClickListener(v -> registrarAtividade("Banhar"));
        cardMamar.setOnClickListener(v -> registrarAtividade("Mamar"));
        cardBrincar.setOnClickListener(v -> registrarAtividade("Brincar"));
        cardRuido.setOnClickListener(v -> registrarAtividade("Ruido branco"));

        return view;
    }

    //quando o usuário clica em algum dia no calendário
    @Override
    protected void onDiaSelecionado(String bebeId, LocalDate data) {

        AtividadeDiaFragment fragment = new AtividadeDiaFragment();

        Bundle bundle = new Bundle();
        bundle.putString("data", data.toString());
        bundle.putString("bebeId", bebeId);

        fragment.setArguments(bundle);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }

    //salvando as atividades
    private void registrarAtividade(String nomeAtividade) {

        if (bebeIdSelecionado == null) {
            Toast.makeText(getContext(), "Selecione um bebê", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> atividade = new HashMap<>();
        atividade.put("nome", nomeAtividade);
        atividade.put("bebeId", bebeIdSelecionado);
        atividade.put("dataHora", com.google.firebase.Timestamp.now());

        db.collection("atividades")
                .add(atividade)
                .addOnSuccessListener(doc ->
                        Toast.makeText(getContext(),
                                nomeAtividade + " registrado",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Erro ao salvar",
                                Toast.LENGTH_SHORT).show()
                );
    }
}