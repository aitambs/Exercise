package com.example.movies;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ArrayConverter {

    @TypeConverter
    public static String fromArray(ArrayList<String> value){
        return new Gson().toJson(value);
    }

    @TypeConverter
    public static ArrayList<String> fromString(String value){
        return new Gson().fromJson(value, new TypeToken<ArrayList<String>>(){}.getType());
    }
}
