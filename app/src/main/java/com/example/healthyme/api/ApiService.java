package com.example.healthyme.api;

import com.example.healthyme.model.Workout;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiService {
    @GET("v1/exercises")
    Call<List<Workout>> getWorkouts(
            @Header("X-Api-Key") String apiKey,
            @Query("muscle") String muscle,
            @Query("difficulty") String difficulty
    );
}
