package com.example.atividadeavaliativa2_progmobile.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.atividadeavaliativa2_progmobile.database.dao.UsuarioDao;
import com.example.atividadeavaliativa2_progmobile.database.dao.PartidaDao;
import com.example.atividadeavaliativa2_progmobile.database.entity.Partida;
import com.example.atividadeavaliativa2_progmobile.database.entity.Usuario;

// classe principal do banco de dados room
@Database(entities = {Usuario.class, Partida.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase{
    public abstract UsuarioDao usuarioDao();
    public abstract PartidaDao partidaDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "Campeonato";

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Usuario ADD COLUMN nickname TEXT");
        }
    };

    public static AppDatabase getDatabase(final Context context){
        if(INSTANCE == null){
            synchronized (AppDatabase.class){
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

