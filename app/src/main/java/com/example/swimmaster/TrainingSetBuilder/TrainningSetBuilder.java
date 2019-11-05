package com.example.swimmaster.TrainingSetBuilder;

public interface TrainningSetBuilder {
    public void resetTraining();

    public void setDays(boolean[] days);

    public void setWeeks(int weeks);

    public void setAdditions(boolean[] additions);

    public void setStartingDistance(int startingDistance);

    public TrainingSet generateTraining();
}