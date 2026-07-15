package com.example.paradise.bebe;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BebeRepository {
    private static final String TAG = "BebeRepository";
    private static final String COLLECTION = "bebes";
    private final FirebaseFirestore db;
    private final String userId;

    public BebeRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    public boolean usuarioLogado() {
        return userId != null;
    }

    // Salvar novo bebê
    public Task<Void> salvarOuAtualizar(BebeModel bebe) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("nome", bebe.getNome());
        dados.put("dataNascimento", bebe.getDataNascimento());
        dados.put("genero", bebe.getGenero());
        dados.put("paiId", userId);
        dados.put("dataCadastro", com.google.firebase.Timestamp.now());

        if (bebe.getId() == null) {
            // Se não tem ID, cria um novo documento
            return db.collection(COLLECTION).document().set(dados);
        } else {
            // Se tem ID, atualiza o existente
            return db.collection(COLLECTION).document(bebe.getId()).update(dados);
        }
    }

    // Buscar um único bebê para edição
    public Task<DocumentSnapshot> buscarPorId(String id) {
        return db.collection(COLLECTION).document(id).get();
    }

    // Deletar bebê
    public Task<Void> deletar(String bebeId) {
        return db.collection(COLLECTION).document(bebeId).delete();
    }

    // Buscar todos os bebês do usuário
    public Task<List<BebeModel>> buscarTodos() {
        return db.collection(COLLECTION)
                .whereEqualTo("paiId", userId)
                .get()
                .continueWith(task -> {
                    List<BebeModel> lista = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            BebeModel bebe = doc.toObject(BebeModel.class);
                            bebe.setId(doc.getId());
                            lista.add(bebe);
                        }
                    }
                    return lista;
                });
    }

    // Listener em tempo real (para o fragment)
    public void ouvirBebes(OnBebesListener listener) {
        db.collection(COLLECTION)
                .whereEqualTo("paiId", userId)
                //atualiza em tempo real
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }

                    List<BebeModel> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : value) {
                        BebeModel bebe = doc.toObject(BebeModel.class);
                        bebe.setId(doc.getId());
                        lista.add(bebe);
                    }
                    listener.onBebesCarregados(lista);
                });
    }

    public interface OnBebesListener {
        void onBebesCarregados(List<BebeModel> bebes);
        void onError(String erro);
    }
}