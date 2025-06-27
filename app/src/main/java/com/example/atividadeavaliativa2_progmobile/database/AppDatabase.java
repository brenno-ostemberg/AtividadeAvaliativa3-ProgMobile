package com.example.atividadeavaliativa2_progmobile.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.atividadeavaliativa2_progmobile.database.dao.UsuarioDao;
import com.example.atividadeavaliativa2_progmobile.database.dao.PartidaDao;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;

// classe principal do banco de dados room
@Database(entities = {Usuario.class, Partida.class}, version = 1, exportSchema = false) //verificar se é version 2 aqui
public abstract class AppDatabase extends RoomDatabase{
    public abstract UsuarioDao usuarioDao();
    public abstract PartidaDao partidaDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "Campeonato";

    /*
    Padrão Singleton:
    - A classe Singleton declara o method estático getInstance que retorna a mesma instância de sua própria classe.
    - O construtor da singleton deve ser escondido do código cliente.
        - Chamando o method getInstance deve ser o único modo de obter o objeto singleton.
    */

    public static AppDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME).build();
                }
            }
        }
        return INSTANCE;
    }
}

