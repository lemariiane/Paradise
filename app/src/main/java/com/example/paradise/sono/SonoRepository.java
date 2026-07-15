package com.example.paradise.sono;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class SonoRepository {
    private static final String TAG = "SonoRepository";
    private static final String COLLECTION_NAME = "sonecas";

    private final FirebaseFirestore db;
    private final CollectionReference collection;

    public SonoRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.collection = db.collection(COLLECTION_NAME);
    }

    // =============== OPERAÇÕES BÁSICAS (CRUD) ===============

    /**
     * Iniciar um novo sono (criar documento com status EM_ANDAMENTO)
     */
    public Task<DocumentReference> iniciarSono(String bebeId) {
        SonoModel novo = new SonoModel(bebeId);
        return collection.add(novo);
    }

    /**
     * Finalizar um sono existente (atualizar com fim, duração e status)
     */
    public Task<Void> finalizarSono(SonoModel sono) {
        sono.finalizar(); // atualiza os campos internos

        return collection.document(sono.getId())
                .update(
                        "fim", sono.getFim(),
                        "status", sono.getStatus(),
                        "duracaoMinutos", sono.getDuracaoMinutos(),
                        "atualizadoEm", sono.getAtualizadoEm()
                );
    }

    /**
     * Finalizar um sono pelo ID (versão simplificada)
     */
    public Task<Void> finalizarSono(String sonoId) {
        // Primeiro busca o sono
        return collection.document(sonoId).get()
                .continueWithTask(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        SonoModel sono = task.getResult().toObject(SonoModel.class);
                        if (sono != null) {
                            sono.setId(task.getResult().getId());
                            sono.finalizar();

                            return collection.document(sonoId).update(
                                    "fim", sono.getFim(),
                                    "status", sono.getStatus(),
                                    "duracaoMinutos", sono.getDuracaoMinutos(),
                                    "atualizadoEm", sono.getAtualizadoEm()
                            );
                        }
                    }
                    return Tasks.forException(new Exception("Sono não encontrado"));
                });
    }
    //salvar o sono de forma manual
    public Task<DocumentReference> salvarSonoManual(SonoModel sono) {
        return collection.add(sono);
    }

    /**
     * Deletar um registro de sono (caso necessário)
     */
    public Task<Void> deletarSono(String sonoId) {
        return collection.document(sonoId).delete();
    }

    // =============== CONSULTAS (QUERIES) ===============

    /**
     * Buscar sono em andamento de um bebê
     */
    public Task<SonoModel> buscarSonoEmAndamento(String bebeId) {
        return collection
                .whereEqualTo("bebeId", bebeId)
                .whereEqualTo("status", "EM_ANDAMENTO")
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        SonoModel sono = doc.toObject(SonoModel.class);
                        if (sono != null) {
                            sono.setId(doc.getId());
                        }
                        return sono;
                    }
                    return null;
                });
    }

    /**
     * Buscar últimas N sonecas de um bebê (finalizadas)
     */
    public Task<List<SonoModel>> buscarUltimasSonecas(String bebeId, int limite) {
        return collection
                .whereEqualTo("bebeId", bebeId)
                .whereEqualTo("status", "FINALIZADO")
                .orderBy("fim", Query.Direction.DESCENDING)
                .limit(limite)
                .get()
                .continueWith(task -> {
                    List<SonoModel> lista = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            SonoModel sono = doc.toObject(SonoModel.class);
                            if (sono != null) {
                                sono.setId(doc.getId());
                                lista.add(sono);
                            }
                        }
                    }
                    return lista;
                });
    }

    /**
     * Buscar todas as sonecas de um bebê em um período
     */
    public Task<List<SonoModel>> buscarSonecasPorPeriodo(String bebeId,
                                                         com.google.firebase.Timestamp inicio,
                                                         com.google.firebase.Timestamp fim) {
        return collection
                .whereEqualTo("bebeId", bebeId)
                .whereGreaterThanOrEqualTo("inicio", inicio)
                .whereLessThanOrEqualTo("inicio", fim)
                .orderBy("inicio", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<SonoModel> lista = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            SonoModel sono = doc.toObject(SonoModel.class);
                            if (sono != null) {
                                sono.setId(doc.getId());
                                lista.add(sono);
                            }
                        }
                    }
                    return lista;
                });
    }

    /**
     * Buscar uma soneca específica pelo ID
     */
    public Task<SonoModel> buscarPorId(String sonoId) {
        return collection.document(sonoId).get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        SonoModel sono = task.getResult().toObject(SonoModel.class);
                        if (sono != null) {
                            sono.setId(task.getResult().getId());
                        }
                        return sono;
                    }
                    return null;
                });
    }

    // =============== LISTENERS EM TEMPO REAL (OPCIONAL) ===============

    /**
     * Ouvir mudanças no sono em andamento de um bebê
     */
    public void ouvirSonoEmAndamento(String bebeId, OnSonoListener listener) {
        collection
                .whereEqualTo("bebeId", bebeId)
                .whereEqualTo("status", "EM_ANDAMENTO")
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        DocumentSnapshot doc = value.getDocuments().get(0);
                        SonoModel sono = doc.toObject(SonoModel.class);
                        if (sono != null) {
                            sono.setId(doc.getId());
                            listener.onSonoEmAndamento(sono);
                        } else {
                            listener.onSonoEmAndamento(null);
                        }
                    } else {
                        listener.onSonoEmAndamento(null);
                    }
                });
    }

    /**
     * Ouvir últimas sonecas de um bebê
     */
    public void ouvirUltimasSonecas(String bebeId, int limite, OnSonosListener listener) {
        collection
                .whereEqualTo("bebeId", bebeId)
                .whereEqualTo("status", "FINALIZADO")
                .orderBy("fim", Query.Direction.DESCENDING)
                .limit(limite)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }

                    List<SonoModel> lista = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            SonoModel sono = doc.toObject(SonoModel.class);
                            if (sono != null) {
                                sono.setId(doc.getId());
                                lista.add(sono);
                            }
                        }
                    }
                    listener.onSonosCarregados(lista);
                });
    }

    public Task<List<SonoModel>> buscarTodosSonos(String bebeId) {
        return db.collection("sonecas")
                .whereEqualTo("bebeId", bebeId)
                .orderBy("inicio", Query.Direction.DESCENDING)//ordenando do mais novo ao mais antigo
                .get()
                .continueWith(task -> {
                    List<SonoModel> lista = new ArrayList<>();
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            SonoModel sono = doc.toObject(SonoModel.class);
                            sono.setId(doc.getId());
                            lista.add(sono);
                        }
                    }
                    return lista;
                });
    }

    // =============== INTERFACES DE CALLBACK ===============

    public interface OnSonoListener {
        void onSonoEmAndamento(SonoModel sono);
        void onError(String erro);
    }

    public interface OnSonosListener {
        void onSonosCarregados(List<SonoModel> sonos);
        void onError(String erro);
    }

    // =============== MÉTODOS UTILITÁRIOS ===============

    /**
     * Verificar se um bebê tem sono em andamento (retorno booleano)
     */
    public Task<Boolean> temSonoEmAndamento(String bebeId) {
        return collection
                .whereEqualTo("bebeId", bebeId)
                .whereEqualTo("status", "EM_ANDAMENTO")
                .limit(1)
                .get()
                .continueWith(task -> task.isSuccessful() && !task.getResult().isEmpty());
    }

    /**
     * Calcular média de sono dos últimos N dias
     */
    public Task<Double> calcularMediaSono(String bebeId, int dias) {
        long segundosEmDias = (long) dias * 24 * 60 * 60;
        com.google.firebase.Timestamp limite =
                new com.google.firebase.Timestamp(
                        (System.currentTimeMillis() / 1000) - segundosEmDias, 0);

        return collection
                .whereEqualTo("bebeId", bebeId)
                .whereEqualTo("status", "FINALIZADO")
                .whereGreaterThanOrEqualTo("inicio", limite)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult().isEmpty()) {
                        return 0.0;
                    }

                    long totalMinutos = 0;
                    int count = 0;

                    for (DocumentSnapshot doc : task.getResult()) {
                        Long duracao = doc.getLong("duracaoMinutos");
                        if (duracao != null) {
                            totalMinutos += duracao;
                            count++;
                        }
                    }

                    return count > 0 ? (totalMinutos / (double) count) / 60.0 : 0.0;
                });
    }

    /**
     * Calcula o horário estimado da próxima soneca
     * baseado na última vez que o bebê acordou + janela de vigília por idade
     */
    public Task<JanelaSono[]> calcularJanelaDeVigilia(String bebeId, int idadeEmSemanas) {
        return buscarUltimasSonecas(bebeId, 1)
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult().isEmpty()) return null;

                    SonoModel ultima = task.getResult().get(0);
                    if (ultima.getFim() == null) return null;

                    long acordouEm = ultima.getFim().toDate().getTime();
                    JanelaSono janela = getJanelaPorIdade(idadeEmSemanas);

                    // Retorna array com [timestampInicio, timestampFim]
                    return new JanelaSono[]{
                            new JanelaSono(janela.getInicioJanelaMs(acordouEm),
                                    janela.getFimJanelaMs(acordouEm))
                    };
                });
    }

    private JanelaSono getJanelaPorIdade(int semanas) {
        if (semanas <= 6)  return new JanelaSono(45, 60);
        if (semanas <= 12) return new JanelaSono(60, 90);
        if (semanas <= 20) return new JanelaSono(90, 120);
        if (semanas <= 32) return new JanelaSono(120, 150);
        if (semanas <= 52) return new JanelaSono(150, 180);
        return new JanelaSono(180, 240);
    }

    public Task<Void> atualizarSono(SonoModel sono) {
        return collection.document(sono.getId())
                .set(sono);
    }
}