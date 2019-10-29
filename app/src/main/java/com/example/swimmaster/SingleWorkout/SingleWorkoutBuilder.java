package com.example.swimmaster.SingleWorkout;

import com.example.swimmaster.Task;

import java.util.List;

public interface SingleWorkoutBuilder {
    public void resetSingleWorkout();
    public void setName(String name);
    public void setDate(String date);
    public void setWarmUp(List<Task> warmUp);
    public void setMainSet(List<Task> mainSet);
    public void setCooldown(List<Task> cooldown);
    public void setCreationdate(String creationDate);
    public SingleWorkout build();
}