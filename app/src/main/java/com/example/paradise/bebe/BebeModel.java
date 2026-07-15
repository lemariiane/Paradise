package com.example.paradise.bebe;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class BebeModel {
    private String id;

    private String nome;

    private Timestamp dataNascimento;

    private String genero;

    private String paiId;

    public BebeModel() {}//construtor vazio

    private Timestamp dataCadastro;

    public Timestamp getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }


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

    public Timestamp getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Timestamp dataNascimento) {
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

    public String getDataNascimentoFormatada() {
        if (dataNascimento == null) return "";

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(dataNascimento.toDate());
    }

    public int getIdadeEmSemanas() {
        if (dataNascimento == null) return 0;

        try {
            Instant nascimentoInstant = dataNascimento.toDate().toInstant();
            LocalDate nascimento = nascimentoInstant
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            LocalDate hoje = LocalDate.now();

            return (int) ChronoUnit.WEEKS.between(nascimento, hoje);
        } catch (Exception e) {
            return 0;
        }
    }
}
