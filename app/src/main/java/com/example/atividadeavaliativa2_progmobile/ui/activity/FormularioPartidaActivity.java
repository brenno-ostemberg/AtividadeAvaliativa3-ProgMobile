package com.example.atividadeavaliativa2_progmobile.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import android.util.Log; // Para logs de depuração

public class FormularioPartidaActivity extends AppCompatActivity {

    private EditText editTextDataPartida;
    private Spinner spinnerJogador1;
    private EditText editTextPlacarJogador1;
    private Spinner spinnerJogador2;
    private EditText editTextPlacarJogador2;
    private Button botaoSalvarPartida;

    private AppDatabase db;
    private List<Usuario> listaCompletaJogadores;
    private ArrayAdapter<String> nicknamesAdapter;

    private ExecutorService executorService;
    private Handler mainThreadHandler;

    // Para armazenar os IDs dos jogadores selecionados
    private int idJogador1Selecionado = -1;
    private int idJogador2Selecionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_partida);

        setTitle("Nova Partida");

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(Looper.getMainLooper());

        editTextDataPartida = findViewById(R.id.editTextDataPartida);
        spinnerJogador1 = findViewById(R.id.spinnerJogador1);
        editTextPlacarJogador1 = findViewById(R.id.editTextPlacarJogador1);
        spinnerJogador2 = findViewById(R.id.spinnerJogador2);
        editTextPlacarJogador2 = findViewById(R.id.editTextPlacarJogador2);
        botaoSalvarPartida = findViewById(R.id.botaoSalvarPartida);

        listaCompletaJogadores = new ArrayList<>();

        nicknamesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        nicknamesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJogador1.setAdapter(nicknamesAdapter);
        spinnerJogador2.setAdapter(nicknamesAdapter);

        configurarListenersSpinners(); // Configurar listeners para os spinners
        carregarJogadoresParaSpinners();

        botaoSalvarPartida.setOnClickListener(view -> salvarPartida());
    }

    private void configurarListenersSpinners() {
        spinnerJogador1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listaCompletaJogadores.size()) {
                    idJogador1Selecionado = listaCompletaJogadores.get(position).getIdJogador();
                } else {
                    idJogador1Selecionado = -1; // Nenhum jogador válido selecionado ou lista vazia
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                idJogador1Selecionado = -1;
            }
        });

        spinnerJogador2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < listaCompletaJogadores.size()) {
                    idJogador2Selecionado = listaCompletaJogadores.get(position).getIdJogador();
                } else {
                    idJogador2Selecionado = -1; // Nenhum jogador válido selecionado ou lista vazia
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                idJogador2Selecionado = -1;
            }
        });
    }

    private void carregarJogadoresParaSpinners() {
        executorService.execute(() -> {
            final List<Usuario> jogadoresDoBanco = db.usuarioDao().getAllJogadores();
            final ArrayList<String> nicknames = new ArrayList<>();

            mainThreadHandler.post(() -> {
                listaCompletaJogadores.clear(); // Limpa a lista antes de adicionar novos
                if (jogadoresDoBanco != null && !jogadoresDoBanco.isEmpty()) {
                    listaCompletaJogadores.addAll(jogadoresDoBanco); // Guarda a lista completa de objetos Jogador
                    for (Usuario usuario : jogadoresDoBanco) {
                        nicknames.add(usuario.getNickname());
                    }
                    nicknamesAdapter.clear();
                    nicknamesAdapter.addAll(nicknames);
                    nicknamesAdapter.notifyDataSetChanged();
                } else {
                    nicknamesAdapter.clear(); // Limpa o adapter se não houver jogadores
                    nicknamesAdapter.notifyDataSetChanged();
                    Toast.makeText(FormularioPartidaActivity.this, "Nenhum jogador cadastrado. Adicione jogadores primeiro.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void salvarPartida() {
        String data = editTextDataPartida.getText().toString().trim();
        String placarStrJogador1 = editTextPlacarJogador1.getText().toString().trim();
        String placarStrJogador2 = editTextPlacarJogador2.getText().toString().trim();

        // --- Validações ---
        if (TextUtils.isEmpty(data)) {
            Toast.makeText(this, "Por favor, insira a data da partida.", Toast.LENGTH_SHORT).show();
            editTextDataPartida.requestFocus();
            return;
        }

        if (idJogador1Selecionado == -1 || spinnerJogador1.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Por favor, selecione o Jogador 1.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idJogador2Selecionado == -1 || spinnerJogador2.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
            Toast.makeText(this, "Por favor, selecione o Jogador 2.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idJogador1Selecionado == idJogador2Selecionado) {
            Toast.makeText(this, "Jogador 1 e Jogador 2 não podem ser a mesma pessoa.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(placarStrJogador1)) {
            Toast.makeText(this, "Por favor, insira o placar do Jogador 1.", Toast.LENGTH_SHORT).show();
            editTextPlacarJogador1.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(placarStrJogador2)) {
            Toast.makeText(this, "Por favor, insira o placar do Jogador 2.", Toast.LENGTH_SHORT).show();
            editTextPlacarJogador2.requestFocus();
            return;
        }

        int placarJogador1;
        int placarJogador2;

        try {
            placarJogador1 = Integer.parseInt(placarStrJogador1);
            placarJogador2 = Integer.parseInt(placarStrJogador2);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Placares devem ser números válidos.", Toast.LENGTH_SHORT).show();
            return;
        }
        // --- Fim das Validações ---

        Partida novaPartida = new Partida();
        novaPartida.Data = data; // Seu campo é 'Data' com 'D' maiúsculo
        novaPartida.idJogador1 = idJogador1Selecionado;
        novaPartida.idJogador2 = idJogador2Selecionado;
        novaPartida.placarJogador1 = placarJogador1;
        novaPartida.placarJogador2 = placarJogador2;

        executorService.execute(() -> {
            try {
                db.partidaDao().inserePartida(novaPartida);
                mainThreadHandler.post(() -> {
                    Toast.makeText(FormularioPartidaActivity.this, "Partida salva com sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Fecha a activity e volta para a lista de partidas
                });
            } catch (Exception e) {
                Log.e("SalvarPartida", "Erro ao salvar partida no banco de dados", e);
                mainThreadHandler.post(() -> Toast.makeText(FormularioPartidaActivity.this, "Erro ao salvar partida.", Toast.LENGTH_SHORT).show());
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