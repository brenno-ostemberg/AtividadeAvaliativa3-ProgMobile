package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.utils.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaJogadoresActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "AppLoginPrefs";
    public static final String KEY_USER_ID = "LOGGED_IN_USER_ID";

    private ListView listViewJogadores;
    private JogadorAdapter adapter;
    private AppDatabase db;
    // Executor para rodar tarefas em background
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler para postar resultados na thread principal (UI)
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_jogadores);

        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        db = AppDatabase.getDatabase(this);
        listViewJogadores = findViewById(R.id.listViewJogadores);

        // Botão para adicionar novo jogador
        FloatingActionButton fab = findViewById(R.id.fabAdicionarJogador);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ListaJogadoresActivity.this, FormularioJogadorActivity.class);
            startActivity(intent);
        });

        // Botão para voltar para main
        ImageButton botaoVoltarMainJogadores = findViewById(R.id.botaoVoltarMainJogadores);
        botaoVoltarMainJogadores.setOnClickListener(v -> {
            Intent intent = new Intent(ListaJogadoresActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        configurarListenersListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarLogin();
        carregarJogadores();
    }

    private void verificarLogin() {
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (!isLoggedIn) {

            Intent intent = new Intent(ListaJogadoresActivity.this, LoginActivity.class);

            // Essas 'flags' são MUITO importantes. Elas dizem ao Android:
            // "Vá para a tela de login E limpe todo o histórico de telas que veio antes."
            // Isso impede que o usuário aperte o botão 'Voltar' no celular e consiga
            // burlar o login, entrando na tela protegida.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            finish();
        }
    }

    private void carregarJogadores() {
        // Executa a busca no banco de dados em uma thread separada
        executorService.execute(() -> {
            // Operação de background
            final List<Usuario> jogadores = db.usuarioDao().getAllJogadores();

            // Posta o resultado para a thread principal para atualizar a UI
            mainThreadHandler.post(() -> {
                adapter = new JogadorAdapter(ListaJogadoresActivity.this, jogadores);
                listViewJogadores.setAdapter(adapter);
            });
        });
    }

    private void configurarListenersListView() {
        // Listener para clique normal (EDITAR)
        listViewJogadores.setOnItemClickListener((parent, view, position, id) -> {

            Usuario usuarioSelecionado = adapter.getItem(position);
            if (usuarioSelecionado != null) {
                Intent intent = new Intent(ListaJogadoresActivity.this, FormularioJogadorActivity.class);
                intent.putExtra("JOGADOR_ID", usuarioSelecionado.getIdJogador());
                startActivity(intent);
            } else {
                // Opcional: Tratar caso onde o item não está mais disponível
                Toast.makeText(this, "Erro ao selecionar jogador.", Toast.LENGTH_SHORT).show();
            }
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
                .setPositiveButton("Sim", (dialog, which) -> excluirJogador(usuario))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirJogador(final Usuario usuario) {
        // Executa a exclusão no banco de dados em uma thread separada
        executorService.execute(() -> {
            // Operação de background
            // A exclusão das partidas associadas é feita automaticamente pelo Room (onDelete = CASCADE)
            db.usuarioDao().excluiJogador(usuario);

            // Posta o resultado para a thread principal para atualizar a UI
            mainThreadHandler.post(() -> {
                // Operação na UI Thread
                Toast.makeText(ListaJogadoresActivity.this, "Jogador excluído!", Toast.LENGTH_SHORT).show();
                carregarJogadores(); // Atualiza a lista
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
