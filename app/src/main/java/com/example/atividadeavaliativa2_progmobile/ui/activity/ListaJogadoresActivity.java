package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Jogador;
import com.example.atividadeavaliativa2_progmobile.utils.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaJogadoresActivity extends AppCompatActivity {

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
        carregarJogadores();
    }

    private void carregarJogadores() {
        // Executa a busca no banco de dados em uma thread separada
        executorService.execute(() -> {
            // Operação de background
            final List<Jogador> jogadores = db.jogadorDao().getAllJogadores();

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

            Jogador jogadorSelecionado = adapter.getItem(position);
            if (jogadorSelecionado != null) {
                Intent intent = new Intent(ListaJogadoresActivity.this, FormularioJogadorActivity.class);
                intent.putExtra("JOGADOR_ID", jogadorSelecionado.getIdJogador());
                startActivity(intent);
            } else {
                // Opcional: Tratar caso onde o item não está mais disponível
                Toast.makeText(this, "Erro ao selecionar jogador.", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para clique longo (EXCLUIR)
        listViewJogadores.setOnItemLongClickListener((parent, view, position, id) -> {
            Jogador jogadorSelecionado = adapter.getItem(position);
            if (jogadorSelecionado != null) {
                exibirDialogoExclusao(jogadorSelecionado);
            } else {
                Toast.makeText(this, "Erro ao selecionar jogador para exclusão.", Toast.LENGTH_SHORT).show();
            }
            return true; // Evento consumido
        });
    }

    private void exibirDialogoExclusao(final Jogador jogador) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Jogador")
                .setMessage("Tem certeza que deseja excluir " + jogador.getNickname() + "? Todas as partidas associadas também serão excluídas.")
                // Usar lambda para simplificar
                .setPositiveButton("Sim", (dialog, which) -> excluirJogador(jogador))
                .setNegativeButton("Não", null)
                .show();
    }

    private void excluirJogador(final Jogador jogador) {
        // Executa a exclusão no banco de dados em uma thread separada
        executorService.execute(() -> {
            // Operação de background
            // A exclusão das partidas associadas é feita automaticamente pelo Room (onDelete = CASCADE)
            db.jogadorDao().excluiJogador(jogador);

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
