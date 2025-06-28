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
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ListaUsuariosActivity extends AppCompatActivity {
    // Variáveis de Interface
    private ListView listViewUsuarios;
    private UsuarioAdapter adapter;

    // Variáveis de Dados e Controle
    private AppDatabase db;
    private SharedPreferences sharedPreferences;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_usuarios);
        setTitle("Lista de Usuários");

        // 1. Inicializa os componentes de controle e interface
        inicializarComponentes();

        // 2. Configura os listeners de clique para os botões e a lista
        configurarListeners();
    }

    private void inicializarComponentes() {
        // Conecta as variáveis da UI aos componentes do layout
        listViewUsuarios = findViewById(R.id.listViewUsuarios);

        // Inicializa os componentes de dados e controle
        db = AppDatabase.getDatabase(this);
        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
    }

    private void configurarListeners() {
        // Configura o listener do Floating Action Button (FAB) para adicionar usuário
        FloatingActionButton fabAdicionarUsuario = findViewById(R.id.fabAdicionarUsuario); // Renomear ID no XML
        fabAdicionarUsuario.setOnClickListener(view -> {
            Intent intent = new Intent(ListaUsuariosActivity.this, FormularioUsuarioActivity.class);
            startActivity(intent);
        });

        // Configura o listener do botão de voltar
        ImageButton botaoVoltarMain = findViewById(R.id.botaoVoltarMain); // Renomear ID no XML
        botaoVoltarMain.setOnClickListener(v -> finish()); // finish() é mais simples se esta tela foi chamada da MainActivity

        // Configura os listeners de clique e clique longo da lista
        listViewUsuarios.setOnItemClickListener((parent, view, position, id) -> editarUsuario(position));
        listViewUsuarios.setOnItemLongClickListener((parent, view, position, id) -> {
            exibirDialogoExclusao(position);
            return true; // Evento consumido
        });
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
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void carregarUsuarios() {
        executorService.execute(() -> {
            try {
                final List<Usuario> jogadores = db.usuarioDao().getAllUsuarios();
                // Lança o resultado na thread principal para atualizar a tela
                mainThreadHandler.post(() -> {
                    adapter = new UsuarioAdapter(ListaUsuariosActivity.this, jogadores);
                    listViewUsuarios.setAdapter(adapter);
                });
            }
            catch (Exception e) {
                mainThreadHandler.post(() -> Toast.makeText(this,
                        "Erro ao carregar usuários.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void editarUsuario(int position) {
        Usuario usuarioSelecionado = adapter.getItem(position);
        if (usuarioSelecionado == null) {
            Toast.makeText(this, "Erro ao selecionar usuário.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, FormularioUsuarioActivity.class);
        intent.putExtra("USUARIO_ID", usuarioSelecionado.getIdUsuario());
        startActivity(intent);
    }

    private void exibirDialogoExclusao(int position) {
        Usuario usuarioSelecionado = adapter.getItem(position);
        if (usuarioSelecionado == null) {
            Toast.makeText(this, "Erro ao selecionar usuário.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Excluir Usuário")
                .setMessage("Tem certeza que deseja excluir " + usuarioSelecionado.getNickname() + "? Todas as partidas associadas também serão excluídas.")
                .setPositiveButton("Sim", (dialog, which) -> excluirUsuario(usuarioSelecionado))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirUsuario(final Usuario usuario) {
        // Remove o usuário da tela imediatamente, sem precisar esperar o banco de dados
        adapter.remove(usuario);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Usuário excluído!", Toast.LENGTH_SHORT).show();

        // Operação real de exclusão em background
        executorService.execute(() -> db.usuarioDao().excluiUsuario(usuario));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
