package com.example.movies;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@androidx.room.Database(entities = {Movie.class}, version = 1)
@TypeConverters({ArrayConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MovieDao movieDao();
}
