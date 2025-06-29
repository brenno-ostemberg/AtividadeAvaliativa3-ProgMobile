package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ListaPartidasActivity extends AppCompatActivity {

    // Variáveis de interface
    private ListView listViewPartidas;
    private EditText editTextFiltroNickname;
    private PartidaAdapter adapter;

    // Variáveis de controle e dados
    private AppDatabase db;
    private SharedPreferences sharedPreferences;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // Variáveis de estado
    private List<Partida> listaDePartidas;
    private Map<Integer, String> mapaNicknamesJogadores;
    private String filtroNicknameAtual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_partidas);

        // 1. Inicializa todos os componentes, adapters e variáveis necessárias
        inicializarComponentes();

        // 2. Configura os listeners de clique para os botões de navegação e da lista
        configurarListeners();

        // 3. Configura os listeners específicos da funcionalidade de filtro
        configurarFiltro();
    }

    private void inicializarComponentes() {
        setTitle("Minhas Partidas");
        db = AppDatabase.getDatabase(this);
        sharedPreferences = getSharedPreferences("login_prefs", Context.MODE_PRIVATE);

        listViewPartidas = findViewById(R.id.listViewPartidas);
        editTextFiltroNickname = findViewById(R.id.editTextFiltroNickname);

        listaDePartidas = new ArrayList<>();
        mapaNicknamesJogadores = new HashMap<>();

        adapter = new PartidaAdapter(this, listaDePartidas, mapaNicknamesJogadores);
        listViewPartidas.setAdapter(adapter);
    }

    private void configurarListeners() {
        FloatingActionButton fabAdicionarPartida = findViewById(R.id.fabAdicionarPartida);
        fabAdicionarPartida.setOnClickListener(view ->
                startActivity(new Intent(this, FormularioPartidaActivity.class))
        );

        ImageButton botaoVoltarMain = findViewById(R.id.botaoVoltarMainPartidas);
        botaoVoltarMain.setOnClickListener(v -> finish());

        // Configura cliques na lista para editar ou excluir
        listViewPartidas.setOnItemClickListener((parent, view, position, id) -> {
            Partida partida = listaDePartidas.get(position);
            Toast.makeText(this, "Editar partida (ainda não implementado)", Toast.LENGTH_SHORT).show();
        });

        listViewPartidas.setOnItemLongClickListener((parent, view, position, id) -> {
            Partida partida = listaDePartidas.get(position);
            exibirDialogoExclusaoPartida(partida);
            return true;
        });
    }

    private void configurarFiltro() {
        Button botaoFiltrar = findViewById(R.id.botaoFiltrarPartidas);
        Button botaoLimparFiltro = findViewById(R.id.botaoLimparFiltroPartidas);

        botaoFiltrar.setOnClickListener(v -> {
            filtroNicknameAtual = editTextFiltroNickname.getText().toString().trim();
            if (!filtroNicknameAtual.isEmpty()) {
                carregarDadosDasPartidas(); // Recarrega com o novo filtro
            } else {
                Toast.makeText(this, "Digite um nickname para filtrar.", Toast.LENGTH_SHORT).show();
            }
        });

        botaoLimparFiltro.setOnClickListener(v -> {
            editTextFiltroNickname.setText("");
            filtroNicknameAtual = null;
            carregarDadosDasPartidas(); // Recarrega com a visão padrão (partidas do usuário logado)
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarLogin();
        carregarDadosDasPartidas();
    }

    private void verificarLogin() {
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);
        if (!isLoggedIn) {
            Intent intent = new Intent(ListaPartidasActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    // Orquestra o carregamento de dados. Pega o ID do usuário logado e inicia a busca de dados
    private void carregarDadosDasPartidas() {
        int usuarioLogadoId = sharedPreferences.getInt("usuario_id", -1);
        if (usuarioLogadoId == -1) {
            Toast.makeText(this, "Erro: sessão de usuário inválida.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Inicia a tarefa em background, passando o ID do usuário logado
        buscarDadosEmBackground(usuarioLogadoId);
    }

    // Executa as consultas ao banco de dados em uma thread separada
    private void buscarDadosEmBackground(int usuarioLogadoId) {
        executorService.execute(() -> {
            try {
                // 1. Busca o mapa de nicknames. Precisaremos dele em qualquer caso.
                Map<Integer, String> nicknames = new HashMap<>();
                for (Usuario u : db.usuarioDao().getAllUsuarios()) {
                    nicknames.put(u.getIdUsuario(), u.getNickname());
                }

                // 2. Decide qual lista de partidas buscar
                List<Partida> partidas;
                if (filtroNicknameAtual != null && !filtroNicknameAtual.isEmpty()) {
                    // Se há um filtro ativo, busca pelo nickname digitado
                    Usuario usuarioFiltrado = db.usuarioDao().encontreUsuarioPorNickName(filtroNicknameAtual);
                    if (usuarioFiltrado != null) {
                        partidas = db.partidaDao().encontrarPartidasPeloIdJogador(usuarioFiltrado.getIdUsuario());
                    } else {
                        mainThreadHandler.post(() -> Toast.makeText(this, "Usuário '" + filtroNicknameAtual + "' não encontrado.", Toast.LENGTH_SHORT).show());
                        partidas = new ArrayList<>(); // Retorna lista vazia se não achou
                    }
                } else {
                    // CASO PADRÃO: Se não há filtro, mostra as partidas do USUÁRIO LOGADO
                    partidas = db.partidaDao().encontrarPartidasPeloIdJogador(usuarioLogadoId);
                }

                // 3. Com os dados prontos, posta para a UI Thread para atualizar a tela
                mainThreadHandler.post(() -> atualizarInterface(partidas, nicknames));

            } catch (Exception e) {
                mainThreadHandler.post(() -> Toast.makeText(this, "Erro ao carregar dados das partidas.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Atualiza o adapter e a UI com os novos dados vindos do banco.
    private void atualizarInterface(List<Partida> partidas, Map<Integer, String> nicknames) {
        listaDePartidas.clear();
        listaDePartidas.addAll(partidas);

        mapaNicknamesJogadores.clear();
        mapaNicknamesJogadores.putAll(nicknames);

        // Notifica o adapter que os dados mudaram, para que ele redesenhe a lista
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
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