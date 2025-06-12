package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.entity.Jogador;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;

import java.util.List;
import java.util.Map;

public class PartidaAdapter extends ArrayAdapter<Partida> {

    private Map<Integer, String> mapaNicknamesJogadores;

    public PartidaAdapter(@NonNull Context context, @NonNull List<Partida> partidas, @NonNull Map<Integer, String> mapaNicknamesJogadores) {
        super(context, 0, partidas);
        this.mapaNicknamesJogadores = mapaNicknamesJogadores;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_partida, parent, false);
        }

        Partida partidaAtual = getItem(position);

        TextView textViewData = itemView.findViewById(R.id.textViewDataPartida);
        TextView textViewNomeJogador1 = itemView.findViewById(R.id.textViewNomeJogador1);
        TextView textViewPlacarJogador1 = itemView.findViewById(R.id.textViewPlacarJogador1);
        TextView textViewNomeJogador2 = itemView.findViewById(R.id.textViewNomeJogador2);
        TextView textViewPlacarJogador2 = itemView.findViewById(R.id.textViewPlacarJogador2);

        if (partidaAtual != null) {
            textViewData.setText("Data: " + partidaAtual.Data); // Acessando o campo Data diretamente

            // Buscando os nicknames do mapa
            String nicknameJogador1 = mapaNicknamesJogadores.getOrDefault(partidaAtual.idJogador1, "Jogador não encontrado");
            String nicknameJogador2 = mapaNicknamesJogadores.getOrDefault(partidaAtual.idJogador2, "Jogador não encontrado");

            textViewNomeJogador1.setText(nicknameJogador1);
            textViewPlacarJogador1.setText(String.valueOf(partidaAtual.placarJogador1));

            textViewNomeJogador2.setText(nicknameJogador2);
            textViewPlacarJogador2.setText(String.valueOf(partidaAtual.placarJogador2));
        }

        return itemView;
    }

    // Método para atualizar o mapa de nicknames se necessário (por exemplo, se os jogadores mudarem)
    public void atualizarNicknames(Map<Integer, String> novoMapaNicknames) {
        this.mapaNicknamesJogadores = novoMapaNicknames;
        notifyDataSetChanged(); // Notifica o adapter que os dados mudaram e a lista precisa ser redesenhada
    }
}
