package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.dao.UsuarioDao;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.google.android.material.textfield.TextInputEditText;

/*
ideia do código: Vamos criar o method que será acionado pelo clique no botão "Salvar".
Este method será responsável por coletar os dados da tela, validá-los, processar a senha (através de hashing),
criar um objeto Usuario e, por fim, usar o UsuarioDao para inserir esse novo objeto no banco de dados.
*/

public class CadastroActivity extends AppCompatActivity {

    /*
    CadastroActivity:
    - campos de nome
    - nickname
    - email
    - senha
    */

    private TextInputEditText campoNome;
    private TextInputEditText campoNickname;
    private TextInputEditText campoEmail;
    private TextInputEditText campoSenha;
    private Button buttonSalvar;
    private UsuarioDao usuarioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //usuarioDao = new UsuarioDao(); não se utiliza dessa forma
        usuarioDao = AppDatabase.getDatabase(getApplicationContext()).usuarioDao();
        EditText campoNome = findViewById(R.id.campo_nome);
        EditText campoNickname = findViewById(R.id.campo_nickname);
        EditText campoEmail = findViewById(R.id.campo_email);
        EditText campoSenha = findViewById(R.id.campo_senha);
        buttonSalvar = findViewById(R.id.button_salvar);

        buttonSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });
    }

    private void cadastrarUsuario() {
        String nome = campoNome.getText().toString().trim();
        String nickname = campoNickname.getText().toString().trim();
        String email = campoEmail.getText().toString().trim();
        String senha = campoSenha.getText().toString();

        if (nome.isEmpty() || nickname.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String senhaHash = PasswordHasher.hashPassword(senha);

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setNickname(nickname);
        novoUsuario.setEmail(email);
        novoUsuario.setSenhaHash(senhaHash);

        //foto do usuário aqui inserção no db
        //logica da foto
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                usuarioDao.insereJogador(novoUsuario);

                // Após a inserção, mostramos uma mensagem de sucesso na UI thread
                runOnUiThread(() -> {
                    Toast.makeText(CadastroActivity.this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CadastroActivity.this, "Erro ao cadastrar. Verifique se o e-mail ou nickname já não estão em uso.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
