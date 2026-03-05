package com.example.paradise.bebe;

public class BebeModel {
    private String id;

    private String nome;

    private String dataNascimento;

    private String genero;

    private String paiId;

    public BebeModel() {}//construtor vazio


    //get e set
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getPaiId() {
        return paiId;
    }

    public void setPaiId(String paiId) {
        this.paiId = paiId;
    }
}
