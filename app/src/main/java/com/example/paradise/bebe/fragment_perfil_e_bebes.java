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
import android.widget.Toast;

import com.example.paradise.LoginActivity;
import com.example.paradise.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class fragment_perfil_e_bebes extends Fragment {

    private RecyclerView recyclerView;
    private BebeAdapter adapter;
    private List<BebeModel> bebeList;
    private FirebaseFirestore db;
    private String paiId;

    public fragment_perfil_e_bebes() {
        //Construtor vazio
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            paiId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil_e_bebes, container, false);

        // 1. Configuração do RecyclerView e Lista
        recyclerView = view.findViewById(R.id.recycler_bebes);
        bebeList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Configuração do Adapter com a ação de clique (Excluir)
        adapter = new BebeAdapter(bebeList, getContext(), (bebe, acao) -> {
            if (acao.equals("EXCLUIR")) {
                confirmarExclusao(bebe);
            }
        });

        //logout
        android.widget.ImageButton btnLogout = view.findViewById(R.id.btn_logout);

        btnLogout.setOnClickListener(v -> {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);

            //limpa todo o histórico de telas anteriores
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            getActivity().finish();

            Toast.makeText(getContext(), "Logoff realizado!", Toast.LENGTH_SHORT).show();
        });

        //Novo Bebê
        Button fab = view.findViewById(R.id.btn_novo_bebe);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CadastroBebeActivity.class);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        //Inicia a escuta em tempo real do banco de dados
        sincronizarDados();

        return view;
    }

    // --- MÉTODOS DE BANCO DE DADOS E LÓGICA ---

    private void sincronizarDados() {
        if (paiId == null) return;

        // Escuta apenas os bebês que pertencem ao pai logado
        db.collection("bebes")
                .whereEqualTo("paiId", paiId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    bebeList.clear();
                    for (DocumentSnapshot doc : value) {
                        BebeModel bebe = doc.toObject(BebeModel.class);
                        if (bebe != null) {
                            bebe.setId(doc.getId()); // Seta o ID do documento no modelo
                            bebeList.add(bebe);
                        }
                    }
                    adapter.notifyDataSetChanged(); // Avisa o Adapter que a lista mudou
                });
    }

   //mensagem para confirmar a exclusão
    private void confirmarExclusao(BebeModel bebe) {
        new AlertDialog.Builder(getContext())
                .setTitle("Deseja remover?")
                .setMessage("Tem certeza que deseja excluir os dados de " + bebe.getNome() + "?")
                .setPositiveButton("Sim, excluir", (dialog, which) -> {
                    removerBebeDoBanco(bebe);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    //excluindo o bebê do firestore
    private void removerBebeDoBanco(BebeModel bebe) {
        db.collection("bebes").document(bebe.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Dados do bebê removidos com sucesso!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erro ao excluir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}