package com.example.runalyzerapp;

import android.content.Context;

public interface RunnerDetection {
    public RunnerInformation detectRunnerInformation(SingleFrame singleFrame, Context context);
}
