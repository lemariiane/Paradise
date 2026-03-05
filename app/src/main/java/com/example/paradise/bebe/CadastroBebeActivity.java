package com.example.paradise.bebe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.paradise.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastroBebeActivity extends AppCompatActivity {

    private TextView tvTitulo;
    private EditText editNomeBebe, editDataNasc;
    private Spinner spinnerGenero;
    private Button btnCadastrar;
    private String bebeIdParaEdicao = null;
    private String userId;
    private FirebaseFirestore db;
    private String[] opcoesGenero = {"Selecione um gênero", "Feminino", "Masculino", "Outro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_bebe);

        // Ajuste de margens do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Inicializações
        db = FirebaseFirestore.getInstance();
        tvTitulo= findViewById(R.id.tv_titulo_cadastro);
        editNomeBebe = findViewById(R.id.nome_bebe);
        editDataNasc = findViewById(R.id.data_nasc);
        spinnerGenero = findViewById(R.id.spinner_genero);
        btnCadastrar = findViewById(R.id.cadastro_bebe_button);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 2. Configurar Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcoesGenero);
        spinnerGenero.setAdapter(adapter);

        configurarCalendario();

        // 3. Verificar se é Edição ou Novo Cadastro
        bebeIdParaEdicao = getIntent().getStringExtra("BEBE_ID");

        if (bebeIdParaEdicao != null) {
            // Segurança para não travar se o tema não tiver
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Perfil");
            }
            tvTitulo.setText("Editar dados do bebê");

            btnCadastrar.setText("Atualizar Dados");
            carregarDadosDoBebe();
        }

        btnCadastrar.setOnClickListener(v -> validarESalvar());
    }

    private void configurarCalendario() {
        editDataNasc.setOnClickListener(v -> {
            final java.util.Calendar c = java.util.Calendar.getInstance();
            int ano = c.get(java.util.Calendar.YEAR);
            int mes = c.get(java.util.Calendar.MONTH);
            int dia = c.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                    (view, year, monthOfYear, dayOfMonth) -> {
                        String dataFormatada = String.format("%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year);
                        editDataNasc.setText(dataFormatada);
                    }, ano, mes, dia);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void validarESalvar() {
        String nome = editNomeBebe.getText().toString().trim();
        String data = editDataNasc.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();

        if (nome.isEmpty()) { editNomeBebe.setError("Digite o nome"); return; }
        if (data.isEmpty()) { Toast.makeText(this, "Selecione uma data", Toast.LENGTH_SHORT).show(); return; }
        if (genero.equals("Selecione um gênero")) { Toast.makeText(this, "Selecione o gênero", Toast.LENGTH_SHORT).show(); return; }

        salvarNoFirestore(nome, data, genero);
    }

    private void salvarNoFirestore(String nome, String data, String genero) {
        //não salvar sem o userId
        if (userId == null) {
            Toast.makeText(this, "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> bebe = new HashMap<>();
        bebe.put("nome", nome);
        bebe.put("dataNascimento", data);
        bebe.put("genero", genero);
        bebe.put("paiId", userId);
        bebe.put("dataCadastro", Timestamp.now());

        if (bebeIdParaEdicao != null) {

            db.collection("bebes")
                    .document(bebeIdParaEdicao)
                    .update(bebe)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Dados atualizados!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show());

        } else {

            db.collection("bebes")
                    .add(bebe)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(this, "Cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao salvar", Toast.LENGTH_SHORT).show());
        }
    }

    private void carregarDadosDoBebe() {
        db.collection("bebes").document(bebeIdParaEdicao).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        editNomeBebe.setText(documentSnapshot.getString("nome"));
                        editDataNasc.setText(documentSnapshot.getString("dataNascimento"));

                        // Lógica para selecionar o gênero no Spinner automaticamente
                        String generoSalvo = documentSnapshot.getString("genero");
                        if (generoSalvo != null) {
                            for (int i = 0; i < opcoesGenero.length; i++) {
                                if (opcoesGenero[i].equals(generoSalvo)) {
                                    spinnerGenero.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }
                });
    }
}