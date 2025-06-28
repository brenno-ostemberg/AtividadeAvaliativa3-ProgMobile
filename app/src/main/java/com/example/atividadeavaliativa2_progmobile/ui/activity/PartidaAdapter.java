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
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;

import java.util.List;
import java.util.Map;

public class PartidaAdapter extends ArrayAdapter<Partida> {

    private final Map<Integer, String> mapaNicknames;

    public PartidaAdapter(@NonNull Context context, @NonNull List<Partida> partidas,
                          @NonNull Map<Integer, String> mapaNicknames) {
        super(context, 0, partidas);
        this.mapaNicknames = mapaNicknames;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Verifica se a view da partida já existe
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_partida, parent, false);
        }

        // Pega o objeto da partida atual
        Partida partidaAtual = getItem(position);

        // Pega as views do layout
        TextView textViewData = convertView.findViewById(R.id.textViewDataPartida);
        TextView textViewNomeJogador1 = convertView.findViewById(R.id.textViewNomeJogador1);
        TextView textViewNomeJogador2 = convertView.findViewById(R.id.textViewNomeJogador2);
        TextView textViewPlacar = convertView.findViewById(R.id.textViewPlacar);

        if (partidaAtual != null) {

            // Busca o nickname do jogador 1 e jogador 2
            String nicknameJogador1 = mapaNicknames.getOrDefault(partidaAtual.idJogador1, "Jogador 1 não encontrado");
            String nicknameJogador2 = mapaNicknames.getOrDefault(partidaAtual.idJogador2, "Jogador 2 não encontrado");

            // Formata o placar e a data
            String placarFormatado = partidaAtual.placarJogador1 + " x " + partidaAtual.placarJogador2;
            String dataFormatada = getContext().getString(R.string.formato_data_partida, partidaAtual.Data);

            // Por fim, preenche as views com os dados
            textViewData.setText(dataFormatada);
            textViewNomeJogador1.setText(nicknameJogador1);
            textViewNomeJogador2.setText(nicknameJogador2);
            textViewPlacar.setText(placarFormatado);
        }
        return convertView;
    }

    /*
    - Metodo para atualizar o mapa de nicknames se necessário (por exemplo, se os jogadores mudarem)
    public void atualizarNicknames(Map<Integer, String> novoMapaNicknames) {
        this.mapaNicknames = novoMapaNicknames;
        notifyDataSetChanged(); // Notifica o adapter que os dados mudaram e a lista precisa ser redesenhada
    }
    */
}
