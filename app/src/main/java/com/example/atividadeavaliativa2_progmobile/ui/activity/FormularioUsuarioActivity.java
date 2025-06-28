package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormularioUsuarioActivity extends AppCompatActivity {

    private EditText editTextNome, editTextNickname, editTextEmail, editTextDataNascimento;
    private AppDatabase db;
    private Usuario usuarioAtual;
    // Executor para rodar tarefas em background
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler para postar resultados na thread principal (UI)
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_usuario);

        db = AppDatabase.getDatabase(this);
        editTextNome = findViewById(R.id.editTextNome);
        editTextNickname = findViewById(R.id.editTextNickname);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextDataNascimento = findViewById(R.id.editTextDataNascimento);
        Button botaoSalvar = findViewById(R.id.botaoSalvarJogador);

        // Verifica se a activity foi chamada para editar um jogador
        if (getIntent().hasExtra("JOGADOR_ID")) {
            setTitle("Editar Jogador");
            int jogadorId = getIntent().getIntExtra("JOGADOR_ID", -1);
            if (jogadorId != -1) {
                carregarJogador(jogadorId);
            } else {
                // Tratar erro: ID inválido passado
                Toast.makeText(this, "Erro ao carregar jogador: ID inválido.", Toast.LENGTH_LONG).show();
                finish(); // Fecha a activity se o ID for inválido
            }
        } else {
            setTitle("Adicionar Jogador");
            usuarioAtual = new Usuario();
        }

        botaoSalvar.setOnClickListener(v -> salvarJogador());
    }

    private void carregarJogador(final int id) {
        executorService.execute(() -> {
            // Operação de background
            final Usuario usuario = db.usuarioDao().getUsuarioById(id);

            // Posta o resultado para a thread principal para atualizar a UI
            mainThreadHandler.post(() -> {
                // Operação na UI Thread
                if (usuario != null) {
                    usuarioAtual = usuario;
                    editTextNome.setText(usuario.getNome());
                    editTextNickname.setText(usuario.getNickname());
                    editTextEmail.setText(usuario.getEmail());
                    editTextDataNascimento.setText(usuario.getDataNascimento());
                } else {
                    // Tratar caso onde o jogador não foi encontrado com o ID fornecido
                    Toast.makeText(FormularioUsuarioActivity.this, "Erro: Jogador não encontrado.", Toast.LENGTH_LONG).show();
                    finish(); // Fecha a activity se não encontrar o jogador para editar
                }
            });
        });
    }

    private void salvarJogador() {
        final String nome = editTextNome.getText().toString().trim();
        final String nickname = editTextNickname.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String dataNascimento = editTextDataNascimento.getText().toString().trim();

        // Validação básica
        if (nome.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, "Nome e Nickname são obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Atualiza o objeto jogadorAtual com os dados do formulário
        // Certifique-se que jogadorAtual não é null (deve ser inicializado no onCreate)
        if (usuarioAtual == null) {
            // Isso não deveria acontecer se a lógica do onCreate estiver correta
            Toast.makeText(this, "Erro interno ao salvar.", Toast.LENGTH_SHORT).show();
            return;
        }
        usuarioAtual.setNome(nome);
        usuarioAtual.setNickname(nickname);
        usuarioAtual.setEmail(email);
        usuarioAtual.setDataNascimento(dataNascimento);

        // Cria uma cópia final do jogador para usar dentro da lambda do executor
        final Usuario usuarioParaSalvar = usuarioAtual;

        executorService.execute(() -> {
            // Operação de background
            boolean sucesso;
            try {
                if (usuarioParaSalvar.getIdUsuario() == 0) { // Novo jogador
                    db.usuarioDao().insereUsuario(usuarioParaSalvar);
                } else { // Jogador existente
                    db.usuarioDao().atualizaUsuario(usuarioParaSalvar);
                }
                sucesso = true;
            } catch (Exception e) {
                // Captura exceção (provavelmente violação de constraint de nickname único)
                sucesso = false;
                // Opcional: Logar o erro e.printStackTrace();
            }

            // Posta o resultado para a thread principal para atualizar a UI
            final boolean finalSucesso = sucesso;
            mainThreadHandler.post(() -> {
                // Operação na UI Thread
                if (finalSucesso) {
                    Toast.makeText(FormularioUsuarioActivity.this, "Jogador salvo!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a activity e volta para a lista
                } else {
                    Toast.makeText(FormularioUsuarioActivity.this, "Erro: Este nickname já existe!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
