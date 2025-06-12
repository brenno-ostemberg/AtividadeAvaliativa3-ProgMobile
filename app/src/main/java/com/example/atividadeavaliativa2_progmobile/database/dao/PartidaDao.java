package com.example.atividadeavaliativa2_progmobile.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;
import java.util.List;

@Dao
public interface PartidaDao {

    @Delete
    void excluiPartida(Partida partida);

    @Insert
    void inserePartida(Partida partida);

    @Update
    void atualizaPartida(Partida partida);

    @Query("SELECT * FROM Partida WHERE idPartida = :idPartida LIMIT 1")
    Partida encontrePartidaPorId(int idPartida);

    @Query("SELECT * FROM Partida WHERE idJogador1 = :idJogador OR idJogador2 = :idJogador")
    List<Partida> encontrarPartidasPeloIdJogador(int idJogador);

    @Query("SELECT * FROM Partida") // Add this annotation
    List<Partida> getAllPartidas();
}
