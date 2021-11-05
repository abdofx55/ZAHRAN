package com.zahran.data;

import android.content.Context;
import android.util.Log;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {PersonEntity.class}, version = 18)
public abstract class Database extends RoomDatabase {
    private static final String LOG_TAG = Database.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "zahran_family_tree.db";
    private static Database sInstance;

    public static Database getInstance(Context context) throws IllegalStateException {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        Database.class, Database.DATABASE_NAME)
                        .createFromAsset("databases/"+DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract PersonDao personDao();
}

