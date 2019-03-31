package com.example.movies;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie ORDER BY year DESC, title ASC")
    List<Movie> getAll();
    
    @Query("Select * FROM movie WHERE uid LIKE :id")
    Movie getMovieById(int id);

    @Insert
    void insertAll(Movie... movies);
}
