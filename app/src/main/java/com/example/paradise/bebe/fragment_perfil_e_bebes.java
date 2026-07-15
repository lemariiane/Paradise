package com.example.paradise.bebe;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paradise.LoginActivity;
import com.example.paradise.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class fragment_perfil_e_bebes extends Fragment {

    private RecyclerView recyclerView;
    private TextView email_usuario;
    private BebeAdapter adapter;
    private BebeRepository bebeRepository;

    public fragment_perfil_e_bebes() {
        // Construtor vazio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_e_bebes, container, false);

        bebeRepository = new BebeRepository();

        if (!bebeRepository.usuarioLogado()) {
            redirecionarParaLogin();
            return view;
        }

        email_usuario= view.findViewById(R.id.txt_email_usuario);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario != null) {
            String email = usuario.getEmail();
            email_usuario.setText(email);
        }

        configurarRecyclerView(view);

        configurarBotoes(view);

        ouvirBebes();

        return view;
    }

    private void configurarRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_bebes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BebeAdapter(new ArrayList<>(), getContext(), (bebe, acao) -> {
            if (acao.equals("EXCLUIR")) {
                confirmarExclusao(bebe);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void configurarBotoes(View view) {
        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            redirecionarParaLogin();
        });

        // Novo bebê
        view.findViewById(R.id.btn_novo_bebe).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CadastroBebeActivity.class));
        });
    }

    private void ouvirBebes() {
        bebeRepository.ouvirBebes(new BebeRepository.OnBebesListener() {
            @Override
            public void onBebesCarregados(List<BebeModel> bebes) {
                adapter.atualizarLista(bebes);
            }


            @Override
            public void onError(String erro) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Erro: " + erro, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void confirmarExclusao(BebeModel bebe) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remover bebê")
                .setMessage("Tem certeza que deseja excluir " + bebe.getNome() + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    bebeRepository.deletar(bebe.getId())
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Removido!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void redirecionarParaLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}