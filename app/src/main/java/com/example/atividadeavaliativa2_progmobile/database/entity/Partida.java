package com.example.atividadeavaliativa2_progmobile.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity (
    foreignKeys = {
        @ForeignKey(
                entity = Usuario.class,
                parentColumns = "idJogador",
                childColumns = "idJogador1",
                onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
                entity = Usuario.class,
                parentColumns = "idJogador",
                childColumns = "idJogador2",
                onDelete = ForeignKey.CASCADE
        )
    }
)
public class Partida {

    @PrimaryKey(autoGenerate = true)
    public int idPartida;

    @ColumnInfo(name = "data")
    public String Data;

    @ColumnInfo(name = "idJogador1")
    public int idJogador1;

    @ColumnInfo(name = "idJogador2")
    public int idJogador2;

    @ColumnInfo(name = "placarJogador1")
    public int placarJogador1;

    @ColumnInfo(name = "placarJogador2")
    public int placarJogador2;
}
