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

import com.example.paradise.MainActivity;
import com.example.paradise.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CadastroBebeActivity extends AppCompatActivity {

    private TextView tvTitulo;
    private EditText editNomeBebe, editDataNasc;
    private Spinner spinnerGenero;
    private Button btnCadastrar;
    private String bebeIdParaEdicao = null;
    private String userId;
    private BebeRepository repository;
    private String[] opcoesGenero = {"Selecione um gênero", "Feminino", "Masculino", "Outro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro_bebe);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvTitulo= findViewById(R.id.tv_titulo_cadastro);
        editNomeBebe = findViewById(R.id.nome_bebe);
        editDataNasc = findViewById(R.id.data_nasc);
        spinnerGenero = findViewById(R.id.spinner_genero);
        btnCadastrar = findViewById(R.id.cadastro_bebe_button);

        repository = new BebeRepository();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }

        //configurar Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opcoesGenero);
        spinnerGenero.setAdapter(adapter);

        configurarCalendario();

        //verificar se é Edição ou Novo Cadastro
        bebeIdParaEdicao = getIntent().getStringExtra("BEBE_ID");

        if (bebeIdParaEdicao != null) {
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

        if (nome.isEmpty()) { editNomeBebe.setError("Digite o nome do bebê"); return; }
        if (data.isEmpty() || genero.equals("Selecione um gênero")) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }


        BebeModel bebe = new BebeModel();
        bebe.setId(bebeIdParaEdicao);
        bebe.setNome(nome);
        bebe.setGenero(genero);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(data);

            Timestamp timestamp = new Timestamp(date);
            bebe.setDataNascimento(timestamp);

        } catch (Exception e) {
            editDataNasc.setError("Data inválida");
            return;
        }


        repository.salvarOuAtualizar(bebe)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Sucesso!", Toast.LENGTH_SHORT).show();

                    if (bebeIdParaEdicao != null) {
                        finish();
                    } else {
                        Intent intent = new Intent(CadastroBebeActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void carregarDadosDoBebe() {
        repository.buscarPorId(bebeIdParaEdicao)
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BebeModel bebe = documentSnapshot.toObject(BebeModel.class);
                        if (bebe != null) {
                            editNomeBebe.setText(bebe.getNome());
                            selecionarGeneroNoSpinner(bebe.getGenero());

                            editDataNasc.setText(bebe.getDataNascimentoFormatada());
                        }
                    }
                });
    }

    private void selecionarGeneroNoSpinner(String genero) {
        for (int i = 0; i < opcoesGenero.length; i++) {
            if (opcoesGenero[i].equals(genero)) {
                spinnerGenero.setSelection(i);
                break;
            }
        }
    }
}