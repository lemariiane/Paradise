package com.example.paradise;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//Firebase Auth
import com.example.paradise.bebe.CadastroBebeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private TextView RedirecionarCadastroText;
    private Button loginButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        RedirecionarCadastroText = findViewById(R.id.RedirecionarCadastroText);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = loginEmail.getText().toString();
                String pass = loginPassword.getText().toString();
                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        String userId = auth.getCurrentUser().getUid();
                                        verificarCadastroBebe(userId);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Erro ao realizar o Login!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        loginPassword.setError("Campos vazios não são permitidos");
                    }
                } else if (email.isEmpty()) {
                    loginEmail.setError("Email não pode está vazio.");
                } else {
                    loginEmail.setError("Por favor, insira um e-mail válido.");
                }
            }
        });
        RedirecionarCadastroText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
            }
        });
    }
    private void verificarCadastroBebe(String userId) {
        db.collection("bebes")
                .whereEqualTo("paiId", userId) //para o SQL
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //o pai já tem um bebê cadastrado
                        if (!task.getResult().isEmpty()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        } else {
                            Intent intent = new Intent(LoginActivity.this, CadastroBebeActivity.class);
                            intent.putExtra("USER_ID", userId);
                            startActivity(intent);
                        }
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Erro ao conectar com o banco.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}