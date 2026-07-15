package com.example.paradise.sono;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class SonoModel {
    @DocumentId
    private String id;
    private String bebeId;
    private Timestamp inicio;
    private Timestamp fim;
    private String status;
    private Long duracaoMinutos;
    private Timestamp criadoEm;
    private Timestamp atualizadoEm;

    // Construtor vazio
    public SonoModel() {
    }

    // Construtor para iniciar um novo sono (sem ID, sem fim, duração nula)
    public SonoModel(String bebeId) {
        this.bebeId = bebeId;
        this.inicio = Timestamp.now();
        this.status = "EM_ANDAMENTO";
        this.criadoEm = Timestamp.now();
        this.atualizadoEm = Timestamp.now();
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBebeId() {
        return bebeId;
    }

    public void setBebeId(String bebeId) {
        this.bebeId = bebeId;
    }

    public Timestamp getInicio() {
        return inicio;
    }

    public void setInicio(Timestamp inicio) {
        this.inicio = inicio;
    }

    public Timestamp getFim() {
        return fim;
    }

    public void setFim(Timestamp fim) {
        this.fim = fim;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Long duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public Timestamp getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Timestamp criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Timestamp getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Timestamp atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }


    //altera o status para "FINALIZADO"
    public void finalizar() {
        this.fim = Timestamp.now();
        this.status = "FINALIZADO";
        this.atualizadoEm = Timestamp.now();

        if (this.inicio != null && this.fim != null) {
            long diffSegundos = this.fim.getSeconds() - this.inicio.getSeconds();
            this.duracaoMinutos = diffSegundos / 60;
        }
    }

    @Override
    public String toString() {
        return "SonoModel{" +
                "id='" + id + '\'' +
                ", bebeId='" + bebeId + '\'' +
                ", inicio=" + inicio +
                ", fim=" + fim +
                ", status='" + status + '\'' +
                ", duracaoMinutos=" + duracaoMinutos +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                '}';
    }
}
