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
    void excluiUsuario(Usuario usuario);

    @Insert
    void insereUsuario(Usuario usuario);

    @Update
    void atualizaUsuario(Usuario usuario);

    @Query("SELECT * FROM Usuario WHERE email = :email LIMIT 1")
    Usuario findByEmail(String email);

    @Query("SELECT * FROM Usuario WHERE nickname = :nickname LIMIT 1")
    Usuario encontreUsuarioPorNickName(String nickname);

    @Insert
    long insereJogadorEPegaId(Usuario usuario);

    @Query("SELECT * FROM Usuario")
    List<Usuario> getAllUsuarios();

    @Query("SELECT * FROM Usuario WHERE idUsuario = :id")
    Usuario getUsuarioById(int id);

    @Query("SELECT * FROM Usuario WHERE idUsuario != :idParaExcluir ORDER BY nickname ASC")
    List<Usuario> getAllExceto(int idParaExcluir);
}
