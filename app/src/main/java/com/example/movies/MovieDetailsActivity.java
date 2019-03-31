package com.example.movies;

import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.MessageFormat;

public class MovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Movie movie = getIntent().getParcelableExtra(MovieListActivity.MOVIE);
        ImageView poster = findViewById(R.id.poster);
        Picasso.get().load(movie.image).into(poster);
        TextView title = findViewById(R.id.movie_title);
        title.setText(MessageFormat.format("{0} ({1})", movie.title, String.valueOf(movie.year)));
        TextView genres = findViewById(R.id.genres);
        for (int i = 0; i < movie.genre.size()-1; i++) {
            genres.append(movie.genre.get(i)+", ");
        }
        genres.append(movie.genre.get(movie.genre.size()-1));
        TextView rating = findViewById(R.id.rating);
        rating.setText(String.valueOf(movie.rating));
    }
}
