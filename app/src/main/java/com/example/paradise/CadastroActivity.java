package com.example.paradise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class CadastroActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText cadastroEmail, cadastroPassword;
    private Button cadastroButton;
    private TextView RedirecionarLoginText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        auth = FirebaseAuth.getInstance();
        cadastroEmail = findViewById(R.id.cadastro_email);
        cadastroPassword = findViewById(R.id.cadastro_password);
        cadastroButton = findViewById(R.id.cadastro_button);
        RedirecionarLoginText = findViewById(R.id.RedirecionarLoginText);

        cadastroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = cadastroEmail.getText().toString().trim();
                String pass = cadastroPassword.getText().toString().trim();

                // Validação de campos
                if (user.isEmpty()) {
                    cadastroEmail.setError("Email não pode está vazio!");
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
                    cadastroEmail.setError("Digite um e-mail válido!");
                    return;
                }
                if (pass.isEmpty()) {
                    cadastroPassword.setError("Senha não pode está vazia!");
                    return;
                }
                if (pass.length() < 6) {
                    cadastroPassword.setError("A senha tem que ter no mínimo 6 caracteres!");
                    return;
                }

                auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                cadastroEmail.setError("Esse e-mail já está sendo usado!");
                                cadastroEmail.requestFocus();
                            } else {
                                Toast.makeText(CadastroActivity.this, "Erro: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });

        RedirecionarLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
            }
        });
    }
}