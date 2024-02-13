package com.example.appwebteck.LocalDB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.appwebteck.LocalDB.Location.LocationDao;
import com.example.appwebteck.LocalDB.Location.LocationTable;

@Database(entities = {LocationTable.class}, version = 1)
public abstract class AppDatabase  extends RoomDatabase {
    public abstract LocationDao locationDao();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getINSTANCE (Context context ){
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "appWebteckDatabase")
                    .allowMainThreadQueries().build();
        }
        return INSTANCE;
    }

}
