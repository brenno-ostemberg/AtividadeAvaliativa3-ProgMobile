package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.dao.UsuarioDao;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.utils.MainActivity;
import com.example.atividadeavaliativa2_progmobile.utils.PasswordHasher;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
Coletar os dados, consultar o banco em background para encontrar o usuário pelo e-mail,
comparar o hash da senha digitada com o hash armazenado e, em caso de sucesso, salvar o ID do usuário
em uma "sessão" (SharedPreferences) e navegar para a tela principal do aplicativo.
*/

public class LoginActivity extends AppCompatActivity {
    private EditText editEmail, editSenha;
    private Button buttonEntrar;
    private TextView tvCadastrar;
    private UsuarioDao usuarioDao;

    // SharedPreferences para salvar o estado de login
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inicializa o SharedPreferences
        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        // 2. Verifica se o usuário já está logado, se sim então joga ele direto para tela principal
        if (verificaLoginAutomatico()) {
            return;
        }

        // 3. Se o usuário não estiver logado, continua com o login
        setContentView(R.layout.activity_login);
        setTitle("Login");

        // 4. Inicializa os componentes
        inicializarComponentes();

        // 5. Configura os listeners
        configurarListeners();
    }

    private boolean verificaLoginAutomatico() {
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void inicializarComponentes() {
        usuarioDao = AppDatabase.getDatabase(getApplicationContext()).usuarioDao();
        editEmail = findViewById(R.id.edit_text_email_login);
        editSenha = findViewById(R.id.edit_text_senha_login);
        buttonEntrar = findViewById(R.id.button_entrar);
        tvCadastrar = findViewById(R.id.text_view_cadastrar);
    }

    private void configurarListeners() {
        buttonEntrar.setOnClickListener(v -> login());

        tvCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "E-mail e senha são obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Busca o usuário
            Usuario usuario = usuarioDao.findByEmail(email);

            // Condição 1: Verifica se o usuário existe
            if (usuario == null) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "E-mail inválido", Toast.LENGTH_SHORT).show());
                return;
            }

            // Condição 2: Verifica se a senha está correta
            if (!PasswordHasher.checkPassword(senha, usuario.getSenhaHash())) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Senha inválida", Toast.LENGTH_SHORT).show());
                return;
            }

            // Login bem-sucedido
            runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();

                // Salva o ID do usuário logado no SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("usuario_id", usuario.getIdUsuario());
                editor.putBoolean("is_logged_in", true);
                editor.apply();

                // Navega para a tela principal
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
