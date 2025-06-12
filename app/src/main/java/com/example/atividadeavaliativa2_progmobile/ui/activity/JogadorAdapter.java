package com.example.atividadeavaliativa2_progmobile.ui.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

import com.example.atividadeavaliativa2_progmobile.R;
import com.example.atividadeavaliativa2_progmobile.database.entity.Jogador;

// Adapter para exibir a lista de jogadores em uma ListView
public class JogadorAdapter extends ArrayAdapter<Jogador> {

    public JogadorAdapter(@NonNull Context context, @NonNull List<Jogador> jogadores) {
        // Usamos 0 como resource ID porque vamos inflar nosso próprio layout customizado (item_jogador.xml)
        super(context, 0, jogadores);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // ViewHolder para otimizar a performance, evitando chamadas repetidas a findViewById
        ViewHolder viewHolder;

        // Verifica se a view está sendo reutilizada, senão infla uma nova
        if (convertView == null) {
            // Infla o layout customizado para cada item da lista
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_jogador, parent, false);

            // Cria um novo ViewHolder e armazena as referências das views internas
            viewHolder = new ViewHolder();
            viewHolder.textViewNome = convertView.findViewById(R.id.textViewNomeJogador);
            viewHolder.textViewNickname = convertView.findViewById(R.id.textViewNicknameJogador);

            // Associa o ViewHolder à view (convertView) usando uma tag
            convertView.setTag(viewHolder);
        } else {
            // Se a view está sendo reutilizada, apenas recupera o ViewHolder associado
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Obtém o objeto Jogador para esta posição
        Jogador jogadorAtual = getItem(position);

        // Preenche as views com os dados do jogador
        // Verifica se o jogadorAtual não é nulo (boa prática)
        if (jogadorAtual != null && viewHolder.textViewNome != null && viewHolder.textViewNickname != null) {
            viewHolder.textViewNome.setText(jogadorAtual.getNome());
            viewHolder.textViewNickname.setText(jogadorAtual.getNickname());
        }
        // else {
            // Opcional: Definir um texto padrão ou tratar o caso de views/jogador nulos
            // viewHolder.textViewNome.setText("Nome indisponível");
            // viewHolder.textViewNickname.setText("Nickname indisponível");
        //}

        // Retorna a view (item da lista) pronta para ser exibida
        return convertView;
    }

    /**
     * Classe interna ViewHolder para armazenar as referências das views de cada item da lista.
     * Isso evita chamadas repetidas a findViewById(), melhorando a performance da ListView.
     */
    private static class ViewHolder {
        TextView textViewNome;
        TextView textViewNickname;
    }
}

