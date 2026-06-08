package com.example.healthyme.api;

import com.example.healthyme.model.Workout;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.QueryMap;

public interface ApiService {
    @GET("v1/exercises")
    Call<List<Workout>> getWorkouts(
            @Header("X-Api-Key") String apiKey,
            @QueryMap Map<String, String> options
    );
}
