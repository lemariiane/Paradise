package com.example.paradise.atividades;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class AtividadeModel {

    @DocumentId
    private String id;

    private String bebeId;
    private String nome;
    private Timestamp dataHora;

    //construtor vazio para o Firebase realizar a reflexão
    public AtividadeModel() {}

    public AtividadeModel(String bebeId, String nome, Timestamp dataHora) {
        this.bebeId = bebeId;
        this.nome = nome;
        this.dataHora = dataHora;
    }

    // GETTERS E SETTERS

    public String getId() {
        return id;
    }

    public String getBebeId() {
        return bebeId;
    }

    public void setBebeId(String bebeId) {
        this.bebeId = bebeId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Timestamp getDataHora() {
        return dataHora;
    }

    public void setDataHora(Timestamp dataHora) {
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "AtividadeModel{" +
                "id='" + id + '\'' +
                ", bebeId='" + bebeId + '\'' +
                ", nome='" + nome + '\'' +
                ", dataHora=" + dataHora +
                '}';
    }
}