package com.example.atividadeavaliativa2_progmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;
import com.example.atividadeavaliativa2_progmobile.utils.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaPartidasActivity extends AppCompatActivity {

    private ListView listViewPartidas;
    private PartidaAdapter adapter;
    private AppDatabase db;
    private List<Partida> listaDePartidas;
    private Map<Integer, String> mapaNicknamesJogadores;

    private ExecutorService executorService;
    private Handler mainThreadHandler;
    private EditText editTextFiltroNickname;
    private Button botaoFiltrarPartidas;
    private Button botaoLimparFiltroPartidas;
    private String filtroNicknameAtual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_partidas);

        setTitle("Lista de Partidas");

        db = AppDatabase.getDatabase(this);
        listViewPartidas = findViewById(R.id.listViewPartidas);
        FloatingActionButton fabAdicionarPartida = findViewById(R.id.fabAdicionarPartida);

        listaDePartidas = new ArrayList<>();
        mapaNicknamesJogadores = new HashMap<>();

        executorService = Executors.newSingleThreadExecutor(); // Cria um executor que usa uma única thread
        mainThreadHandler = new Handler(Looper.getMainLooper()); // Handler associado à thread principal

        adapter = new PartidaAdapter(this, listaDePartidas, mapaNicknamesJogadores);
        listViewPartidas.setAdapter(adapter);

        fabAdicionarPartida.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListaPartidasActivity.this, FormularioPartidaActivity.class);
                startActivity(intent);
            }
        });

        ImageButton botaoVoltarMainPartidas = findViewById(R.id.botaoVoltarMainPartidas);
        botaoVoltarMainPartidas.setOnClickListener(v -> {
            Intent intent = new Intent(ListaPartidasActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        configurarListenersListView();

        //filtrar partidas
        editTextFiltroNickname = findViewById(R.id.editTextFiltroNickname);
        botaoFiltrarPartidas = findViewById(R.id.botaoFiltrarPartidas);
        botaoLimparFiltroPartidas = findViewById(R.id.botaoLimparFiltroPartidas);

        botaoFiltrarPartidas.setOnClickListener(v -> {
            filtroNicknameAtual = editTextFiltroNickname.getText().toString().trim();
            if (!filtroNicknameAtual.isEmpty()) {
                carregarDadosDasPartidas(); // Recarrega com o filtro
            } else {
                Toast.makeText(ListaPartidasActivity.this, "Digite um nickname para filtrar.", Toast.LENGTH_SHORT).show();
            }
        });

        botaoLimparFiltroPartidas.setOnClickListener(v -> {
            editTextFiltroNickname.setText("");
            filtroNicknameAtual = null;
            carregarDadosDasPartidas(); // Recarrega mostrando todas as partidas
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDadosDasPartidas();
    }


    private void carregarDadosDasPartidas() {
        executorService.execute(() -> {
            List<Partida> partidasDoBanco = null;
            Map<Integer, String> nicknamesDoBanco = new HashMap<>();
            boolean sucesso = false;

            try {
                // Lógica de filtro
                if (filtroNicknameAtual != null && !filtroNicknameAtual.isEmpty()) {
                    Usuario usuarioFiltrado = db.usuarioDao().encontreJogadorPorNickName(filtroNicknameAtual);
                    if (usuarioFiltrado != null) {
                        partidasDoBanco = db.partidaDao().encontrarPartidasPeloIdJogador(usuarioFiltrado.getIdJogador());
                    } else {
                        partidasDoBanco = new ArrayList<>(); // Nickname não encontrado, lista vazia
                        mainThreadHandler.post(()-> Toast.makeText(ListaPartidasActivity.this, "Jogador '" + filtroNicknameAtual + "' não encontrado.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    // Sem filtro, carregar todas as partidas
                    partidasDoBanco = db.partidaDao().getAllPartidas();
                }

                // Obter nicknames para todas as partidas exibidas (ou todos os jogadores se for mais fácil)
                List<Usuario> todosJogadores = db.usuarioDao().getAllJogadores();
                for (Usuario usuario : todosJogadores) {
                    nicknamesDoBanco.put(usuario.getIdJogador(), usuario.getNickname());
                }
                sucesso = true;
            } catch (Exception e) {
                android.util.Log.e("ListaPartidas", "Erro ao carregar dados", e);
                sucesso = false;
            }

            final List<Partida> finalPartidasDoBanco = partidasDoBanco;
            final Map<Integer, String> finalNicknamesDoBanco = nicknamesDoBanco;
            final boolean finalSucesso = sucesso;

            mainThreadHandler.post(() -> {
                if (finalSucesso && finalPartidasDoBanco != null) {
                    listaDePartidas.clear();
                    listaDePartidas.addAll(finalPartidasDoBanco);

                    mapaNicknamesJogadores.clear();
                    mapaNicknamesJogadores.putAll(finalNicknamesDoBanco); // Garante que o mapa da activity esteja atualizado

                    adapter.notifyDataSetChanged();
                } else if (!finalSucesso) { // Apenas mostrar erro se falhou, não se a lista filtrada for vazia por jogador não encontrado
                    Toast.makeText(ListaPartidasActivity.this, "Erro ao carregar partidas.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // Excluir partida
    private void configurarListenersListView() {
        listViewPartidas.setOnItemClickListener((parent, view, position, id) -> {
            Partida partidaSelecionada = listaDePartidas.get(position);
            // Lógica para editar - ainda não implementada
            // Intent intent = new Intent(ListaPartidasActivity.this, FormularioPartidaActivity.class);
            // intent.putExtra("PARTIDA_ID", partidaSelecionada.idPartida);
            // startActivity(intent);
            Toast.makeText(this, "Editar partida (ID): " + partidaSelecionada.idPartida, Toast.LENGTH_SHORT).show();
        });

        listViewPartidas.setOnItemLongClickListener((parent, view, position, id) -> {
            Partida partidaSelecionada = listaDePartidas.get(position);
            exibirDialogoExclusaoPartida(partidaSelecionada); // Chamar o diálogo
            return true; // Evento consumido
        });
    }

    private void exibirDialogoExclusaoPartida(final Partida partida) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Partida")
                .setMessage("Tem certeza que deseja excluir a partida de " + partida.Data + "?")
                .setPositiveButton("Sim", (dialog, which) -> excluirPartidaDoBanco(partida))
                .setNegativeButton("Não", null)
                .setIcon(android.R.drawable.ic_dialog_alert) // Opcional: adicionar um ícone
                .show();
    }

    private void excluirPartidaDoBanco(final Partida partida) {
        executorService.execute(() -> {
            try {
                db.partidaDao().excluiPartida(partida);
                mainThreadHandler.post(() -> {
                    Toast.makeText(ListaPartidasActivity.this, "Partida excluída!", Toast.LENGTH_SHORT).show();
                    carregarDadosDasPartidas(); // Atualiza a lista
                });
            } catch (Exception e) {
                // Logar o erro
                mainThreadHandler.post(() -> Toast.makeText(ListaPartidasActivity.this, "Erro ao excluir partida.", Toast.LENGTH_SHORT).show());
            }
        });
    }
}