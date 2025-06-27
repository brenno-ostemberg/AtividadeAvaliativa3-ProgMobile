package com.example.atividadeavaliativa2_progmobile.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Index;

@Entity(
    indices = {@Index(value = {"nickname"}, unique = true)}
)
public class Usuario {

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

    @ColumnInfo(name = "senhaHash")
    private String senhaHash;

    @ColumnInfo(name = "uriFoto")
    private String uriFoto; //salva o caminho, não o binário

    public Usuario() {
    }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

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


