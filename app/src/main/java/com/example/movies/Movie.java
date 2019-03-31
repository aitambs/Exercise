package com.example.movies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@Entity (indices = {@Index(value = {"title","year"}, unique = true)})
public class Movie implements Parcelable {

    public Movie(){}

    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String title;

    public String image;

    public float rating;

    @SerializedName("releaseYear")
    public int year;

    public ArrayList<String> genre;

    protected Movie(Parcel in) {
        uid = in.readInt();
        title = in.readString();
        image = in.readString();
        rating = in.readFloat();
        year = in.readInt();
        genre = in.createStringArrayList();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(title);
        dest.writeString(image);
        dest.writeFloat(rating);
        dest.writeInt(year);
        dest.writeStringList(genre);
    }
}
