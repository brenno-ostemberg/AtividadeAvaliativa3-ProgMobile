package com.example.atividadeavaliativa2_progmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.AppDatabase;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class FormularioPartidaActivity extends AppCompatActivity {

    // Variáveis de UI
    private EditText editTextDataPartida;
    private TextView textViewJogador1Nome; //  Substituto do spinnerJogador1
    private EditText editTextPlacarJogador1;
    private Spinner spinnerOponente; // Substituto do spinnerJogador2
    private EditText editTextPlacarJogador2;
    private Button botaoSalvarPartida;

    // Variáveis de Dados e Controle
    private AppDatabase db;
    private SharedPreferences sharedPreferences;
    private ExecutorService executorService;
    private Handler mainThreadHandler;

    // Variáveis que guardam o estado da tela
    private Usuario usuarioLogado;
    private ArrayAdapter<Usuario> oponentesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Aponta para o Layout XML
        setContentView(R.layout.activity_formulario_partida);
        setTitle("Nova Partida");

        // 2. Inicialização dos componentes de controle
        db = AppDatabase.getDatabase(this);
        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        // 3. Conecta as variáveis de UI aos IDs dos componentes
        editTextDataPartida = findViewById(R.id.editTextDataPartida);
        textViewJogador1Nome = findViewById(R.id.text_view_jogador1_nome);
        editTextPlacarJogador1 = findViewById(R.id.editTextPlacarJogador1);
        spinnerOponente = findViewById(R.id.spinner_oponente);
        editTextPlacarJogador2 = findViewById(R.id.editTextPlacarJogador2);
        botaoSalvarPartida = findViewById(R.id.botaoSalvarPartida);

        // 4. Configura o Adapter do spinner de oponente
        oponentesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        oponentesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOponente.setAdapter(oponentesAdapter);

        // 5. Carrega os dados na tela
        carregarDadosIniciais();

        // 6. Por fim, salva as configurações do listener
        botaoSalvarPartida.setOnClickListener(view -> salvarPartida());
    }

    private void carregarDadosIniciais() {
        // 1. Pega o ID do usuário logado
        int idUsuarioLogado = sharedPreferences.getInt("id_usuario_logado", -1);

        // 2. Se não existe ID então já encerra a tela
        if (idUsuarioLogado == -1) {
            Toast.makeText(this, "Erro: Usuário não identificado.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 3. Busca os dados em background (Usuario logado e lista de oponentes)
        executorService.execute(() -> {
            usuarioLogado = db.usuarioDao().getUsuarioById(idUsuarioLogado);
            List<Usuario> oponentes = db.usuarioDao().getAllExceto(idUsuarioLogado);

            // 6. Manda atualizar a UI na thread principal
            mainThreadHandler.post(() -> {
                // Se o ID da sessão for inválido, encerra.
                if (usuarioLogado == null) {
                    Toast.makeText(this, "Erro ao carregar dados do usuário.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // 7. ATUALIZA A UI:
                // a) Mostra o nome do usuário logado no TextView
                textViewJogador1Nome.setText(usuarioLogado.getNickname());

                // b) Completa o spinner com a lista de oponentes
                oponentesAdapter.clear();
                oponentesAdapter.addAll(oponentes);
                oponentesAdapter.notifyDataSetChanged();

                // 8. Lida com o caso de não haver oponentes
                if(oponentes.isEmpty()){
                    Toast.makeText(this, "Nenhum oponente disponível para jogar.",
                            Toast.LENGTH_LONG).show();
                    botaoSalvarPartida.setEnabled(false); // Desabilita o botão salvar
                }
            });
        });
    }

    private void salvarPartida() {
        String data = editTextDataPartida.getText().toString().trim();
        String placarStrJogador1 = editTextPlacarJogador1.getText().toString().trim();
        String placarStrJogador2 = editTextPlacarJogador2.getText().toString().trim();

        // Validações:
        // 1. Valida se o usuário está logado
        if (usuarioLogado == null) {
            Toast.makeText(this, "Aguarde o carregamento dos dados.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Pega o oponente que foi selecionado no spinner
        Usuario oponenteSelecionado = (Usuario) spinnerOponente.getSelectedItem();

        // 2. Valida se o usuário selecionou um oponente
        if (oponenteSelecionado == null) {
            Toast.makeText(this, "Selecione um oponente.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Valida se os campos foram preenchidos
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(placarStrJogador1) || TextUtils.isEmpty(placarStrJogador2)) {
            Toast.makeText(this,"Todos os campos são obrigatórios.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Valida se os placares são números válidos
        int placarJogador1, placarJogador2;
        try {
            placarJogador1 = Integer.parseInt(placarStrJogador1);
            placarJogador2 = Integer.parseInt(placarStrJogador2);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Placares devem ser números.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Após todas as validações, monta o objeto da partida com os dados corretos
        Partida novaPartida = new Partida();
        novaPartida.Data = data;
        novaPartida.idJogador1 = usuarioLogado.getIdUsuario(); // ID do usuário logado
        novaPartida.idJogador2 = oponenteSelecionado.getIdUsuario(); // ID do oponente selecionado
        novaPartida.placarJogador1 = placarJogador1;
        novaPartida.placarJogador2 = placarJogador2;

        // Por fim, tenta salvar a partida
        executorService.execute(() -> {
            try {
                db.partidaDao().inserePartida(novaPartida);
                mainThreadHandler.post(() -> {
                    Toast.makeText(FormularioPartidaActivity.this,
                    "Partida salva!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                mainThreadHandler.post(() -> Toast.makeText(FormularioPartidaActivity.this,
                        "Erro ao salvar partida.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}