package com.example.letter.AddUserRoomArchitecture;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {AddUserEntity.class}, version = 1, exportSchema = false)
public abstract class AddUserDatabase extends RoomDatabase {

    public abstract AddUserDao addUserDao();

    private static volatile AddUserDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 6;

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AddUserDatabase getInstance(final Context context){
        if (INSTANCE == null){
            synchronized (AddUserDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),AddUserDatabase.class,"USER_CONTACT_DATABASE")
                            .addCallback(sRoomDBCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static final RoomDatabase.Callback sRoomDBCallback = new Callback() {
        @Override
        public void onCreate(@NonNull @NotNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                AddUserDao dao = INSTANCE.addUserDao();
                dao.deleteAll();
            });
        }
    };
}
