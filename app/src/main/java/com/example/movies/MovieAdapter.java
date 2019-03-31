package com.example.movies;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieViewHolder> {

    List<Movie> data;
    View.OnClickListener listener;

    MovieAdapter(List<Movie> data, View.OnClickListener clickListener) {
        this.data = data;
        this.listener = clickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movielist_item,parent,false);
        view.setOnClickListener(listener);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        holder.baseView.setTag(data.get(position).uid);
        holder.movieTitle.setText(MessageFormat.format("{0} ({1})", data.get(position).title, String.valueOf(data.get(position).year)));
    }


    @Override
    public int getItemCount() {
        return data.size();
    }
}

class MovieViewHolder extends RecyclerView.ViewHolder {
    TextView movieTitle;
    View baseView;
    MovieViewHolder(@NonNull View itemView) {
        super(itemView);
        baseView=itemView;
        movieTitle = itemView.findViewById(R.id.movie_title);
    }
}
