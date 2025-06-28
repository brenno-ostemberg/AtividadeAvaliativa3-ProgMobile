package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.utils.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ListaUsuariosActivity extends AppCompatActivity {

    private ListView listViewJogadores;
    private UsuarioAdapter adapter;
    private AppDatabase db;
    // Executor das tarefas em background
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler para postar resultados na thread principal (UI)
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);

        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        db = AppDatabase.getDatabase(this);
        listViewJogadores = findViewById(R.id.listViewJogadores);

        // Botão para adicionar novo usuario
        FloatingActionButton fab = findViewById(R.id.fabAdicionarJogador);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ListaUsuariosActivity.this, FormularioUsuarioActivity.class);
            startActivity(intent);
        });

        // Botão para voltar para main
        ImageButton botaoVoltarMainJogadores = findViewById(R.id.botaoVoltarMainJogadores);
        botaoVoltarMainJogadores.setOnClickListener(v -> {
            Intent intent = new Intent(ListaUsuariosActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        configurarListenersListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarLoginDoUsuario();
        carregarUsuarios();
    }

    private void verificarLoginDoUsuario() {
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {
            Intent intent = new Intent(ListaUsuariosActivity.this, LoginActivity.class);

            // Essas 'flags' são MUITO importantes. Elas dizem ao Android:
            // "Vá para a tela de login E limpe totalmente o histórico de telas que veio antes."
            // Isso impede que o usuário aperte o botão 'Voltar' no celular e consiga
            // burlar o login, entrando na tela protegida.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        }
    }

    private void carregarUsuarios() {
        // Executa a busca no banco de dados em uma thread separada
        executorService.execute(() -> {
            // Operação de background
            final List<Usuario> jogadores = db.usuarioDao().getAllUsuarios();

            // Posta o resultado para a thread principal para atualizar a tela
            mainThreadHandler.post(() -> {
                adapter = new UsuarioAdapter(ListaUsuariosActivity.this, jogadores);
                listViewJogadores.setAdapter(adapter);
            });
        });
    }

    private void configurarListenersListView() {
        // Listener para clique normal (EDITAR)
        listViewJogadores.setOnItemClickListener((parent, view, position, id) -> {

            Usuario usuarioSelecionado = adapter.getItem(position);

            if (usuarioSelecionado == null) {
                Toast.makeText(this, "Erro ao selecionar jogador.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ListaUsuariosActivity.this, FormularioUsuarioActivity.class);
            intent.putExtra("JOGADOR_ID", usuarioSelecionado.getIdUsuario());
            startActivity(intent);
        });

        // Listener para clique longo (EXCLUIR)
        listViewJogadores.setOnItemLongClickListener((parent, view, position, id) -> {
            Usuario usuarioSelecionado = adapter.getItem(position);
            if (usuarioSelecionado != null) {
                exibirDialogoExclusao(usuarioSelecionado);
            } else {
                Toast.makeText(this, "Erro ao selecionar jogador para exclusão.", Toast.LENGTH_SHORT).show();
            }
            return true; // Evento consumido
        });
    }

    private void exibirDialogoExclusao(final Usuario usuario) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Jogador")
                .setMessage("Tem certeza que deseja excluir " + usuario.getNickname() + "? Todas as partidas associadas também serão excluídas.")
                // Usar lambda para simplificar
                .setPositiveButton("Sim", (dialog, which) -> excluirUsuario(usuario))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirUsuario(final Usuario usuario) {
        // Remove o usuário da tela imediatamente, sem precisar esperar o banco de dados
        if (adapter != null) {
            adapter.remove(usuario);
            adapter.notifyDataSetChanged();
        }
        Toast.makeText(ListaUsuariosActivity.this, "Jogador excluído!", Toast.LENGTH_SHORT).show();

        // Executa a exclusão real no banco de dados em background
        executorService.execute(() -> db.usuarioDao().excluiUsuario(usuario));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
