package com.example.runalyzerapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class VideoSequence {
    private Uri videoUri;
    private int relativeCreationTime;
    private List<SingleFrame> singleFrames = new ArrayList<>();

    public VideoSequence(Uri videoUri, int relativeCreationTime) {
        this.videoUri = videoUri;
        this.relativeCreationTime = relativeCreationTime;
    }

    public List<SingleFrame> getSingleFrames(){
        return singleFrames;
    }

    public void separateToFrames(Context context){
        VideoFrameProcessor videoFrames = new VideoFrameProcessor();
        //videoFrames.videoToFrames(context, videoUri, relativeCreationTime);

        videoFrames.extractAllFrames(context, videoUri, relativeCreationTime);

        singleFrames = videoFrames.getFrameList();
    }

    public void detectRunnerInformation(){
        Log.d("Benni", "Detecting Runner Information");
        Mat backgroundFrame = singleFrames.get(0).getFrame();
        for(SingleFrame frame : singleFrames){
            frame.detectRunnerInformation(backgroundFrame);
        }
        Log.d("Benni", "Runner Information detected");
    }

    public int getMaxRunnerWidth(){
        int maxRunnerWidth = 0;
        for(SingleFrame frame : singleFrames){
            if(frame.getRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = frame.getRunnerWidth();
            }
        }
        return maxRunnerWidth;
    }

    public int getMaxRunnerHeight(){
        int maxRunnerHeight = 0;
        for(SingleFrame frame : singleFrames){
            if(frame.getRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = frame.getRunnerHeight();
            }
        }
        return maxRunnerHeight;
    }

    public void cropFrames(int width, int height){
        for(SingleFrame frame : singleFrames){
            frame.cropFrame(width, height);
        }
    }
}
