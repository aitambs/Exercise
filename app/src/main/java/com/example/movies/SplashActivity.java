package com.example.movies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    public static final String MOVIES = "Movies";
    Handler handler = new Handler();
    ArrayList<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "movie").build();

                MovieDao movieDao = db.movieDao();

                movies =new ArrayList<>(movieDao.getAll());

                if (movies.isEmpty()) {
                    Log.i(MOVIES,"Database is empty!");
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://api.androidhive.info/json/movies.json").build();
                    String responseBody = null;
                    try {
                        Response response = client.newCall(request).execute();
                        responseBody = response.body().string();
                        movies = new Gson().fromJson(responseBody, new TypeToken<ArrayList<Movie>>(){}.getType());
                        movieDao.insertAll(movies.toArray(new Movie[0]));
                        movies = new ArrayList<>(movieDao.getAll());
                    } catch (IOException e) {
                        Log.e(MOVIES,"Connection Error!");
                        return;
                    }
                }

                db.close();

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, MovieListActivity.class);
                        intent.putExtra(MOVIES,movies);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);
            }
        });

        thread.start();
    }
}
