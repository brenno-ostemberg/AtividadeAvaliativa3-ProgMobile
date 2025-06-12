package com.example.atividadeavaliativa2_progmobile.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

@Entity(
    indices = {@Index(value = {"nickname"}, unique = true)}
)
public class Jogador {

    @PrimaryKey(autoGenerate = true)
    public int idJogador;

    @ColumnInfo(name = "nome")
    public String nome;

    @ColumnInfo(name = "nickname")
    public String nickname;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "dataNascimento")
    public String dataNascimento;

    public Jogador() {
    }

    public int getIdJogador() {
        return idJogador;
    }

    public void setIdJogador(int idJogador) {
        this.idJogador = idJogador;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}


