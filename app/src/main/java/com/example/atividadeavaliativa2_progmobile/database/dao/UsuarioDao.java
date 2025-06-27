package com.example.atividadeavaliativa2_progmobile.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Delete
    void excluiJogador(Usuario usuario);

    @Insert
    void insereJogador(Usuario usuario);

    @Update
    void atualizaJogador(Usuario usuario);

    //verificar essa implementação depois
    default Usuario findByEmail(String email) {
        return null;
    }

    @Query("SELECT * FROM Usuario WHERE nickname = :nickname LIMIT 1")
    Usuario encontreJogadorPorNickName(String nickname);

    @Insert
    long insereJogadorEPegaId(Usuario usuario);

    @Query("SELECT * FROM Usuario")
    List<Usuario> getAllJogadores();

    @Query("SELECT * FROM Usuario WHERE idJogador = :id")
    Usuario getJogadorById(int id);

}
