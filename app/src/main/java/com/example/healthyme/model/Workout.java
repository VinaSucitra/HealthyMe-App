package com.example.healthyme.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Workout implements Serializable {
    @SerializedName("name")
    private String name;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("muscle")
    private String muscle;
    
    @SerializedName("difficulty")
    private String difficulty;
    
    @SerializedName("instructions")
    private String instructions;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMuscle() { return muscle; }
    public void setMuscle(String muscle) { this.muscle = muscle; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
