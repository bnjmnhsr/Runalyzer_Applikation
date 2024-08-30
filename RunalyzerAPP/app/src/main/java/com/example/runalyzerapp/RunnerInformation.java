package com.example.runalyzerapp;

public class RunnerInformation {
    private Vector runnerPosition;
    private Vector correctedRunnerPosition; //xPos gets smoothed
    private int runnerWidth;
    private int runnerHeight;

    public RunnerInformation(){
        this.runnerPosition = new Vector();
        this.correctedRunnerPosition = new Vector();
        this.runnerWidth = 0;
        this.runnerHeight = 0;
    }

    public RunnerInformation(Vector runnerPosition, int runnerWidth, int runnerHeight){
        this.runnerPosition = runnerPosition;
        this.correctedRunnerPosition = runnerPosition;
        this.runnerWidth = runnerWidth;
        this.runnerHeight = runnerHeight;
    }

    public Vector getRunnerPosition(){
        return runnerPosition;
    }

    public Vector getCorrectedRunnerPosition(){
        return correctedRunnerPosition;
    }

    public int getRunnerWidth(){
        return runnerWidth;
    }

    public int getRunnerHeight(){
        return runnerHeight;
    }

    public void setEmptyRunnerInformation(){
        runnerPosition = new Vector();
        runnerWidth = 0;
        runnerHeight = 0;
    }

    public void correctRunnerPosition(Vector runnerPosition){
        correctedRunnerPosition = runnerPosition;
    }
}
