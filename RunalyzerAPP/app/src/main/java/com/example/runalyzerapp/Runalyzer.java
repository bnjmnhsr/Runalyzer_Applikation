package com.example.runalyzerapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Runalyzer {
    private List<Uri> inputVideoUris;
    private List<VideoSequence> videoSequences;
    private int[] millisCreationTime;
    private int maxRunnerWidth;
    private int maxRunnerHeight;
    private int middleYPosition;
    private FinalVideo finalVideo;

    public Runalyzer(List<Uri> inputVideoUris, int[] millisCreationTime){
        this.maxRunnerWidth = 0;
        this.maxRunnerHeight = 0;
        this.inputVideoUris = inputVideoUris;
        this.millisCreationTime = millisCreationTime;
    }

    public String detectRunnerInformation(Context context){
        String retval = null;
        videoSequences = new ArrayList<>();
        int totalMillisAllVideos = 0;

        if(inputVideoUris.isEmpty()){
            Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerInformation(): No input videos (Uris size = 0)");
            return ("No input videos.");
        }
        for (int i = 0; i < inputVideoUris.size(); i++) {
            Uri videoUri = inputVideoUris.get(i);
            if(millisCreationTime == null){
                Log.d("RunalyzerDEBUG","Runalyzer: detectRunnerInformation(): millisCreationTime == null");
                return ("Creation time in milliseconds is a null value array");
            }else if(i > (millisCreationTime.length - 1)){
                Log.d("RunalyzerDEBUG","Runalyzer: detectRunnerInformation(): No millisCreationTime for video " + i);
                return ("No creation time in milliseconds available for video " + i);
            }
            videoSequences.add(new VideoSequence(context, videoUri, millisCreationTime[i]));
            totalMillisAllVideos += videoSequences.get(i).getVideoDurationInMillis();
        }
        for(VideoSequence vidSeq : videoSequences){
            vidSeq.setDetectionMethod(new BackgroundSubtraction());
            retval = vidSeq.separateToFrames(totalMillisAllVideos);
            if(!Objects.equals(retval, "success")){
                return retval;
            }
            retval = vidSeq.smoothXPos();
            if(!Objects.equals(retval, "success")){
                return retval;
            }
        }
        return retval;
    }

    public String detectRunnerDimensions(){
        if(videoSequences.isEmpty()){
            Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerDimensions(): No video sequences");
            return ("No video sequences to detect max runner width and height.");
        }

        int highestYPosition = 0;
        int lowestYPosition = videoSequences.get(0).getSelectedSingleFrames().get(0).getFrame().height();
        if(lowestYPosition == 0){
            Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerDimensions(): Frame-Height of 0 detected, error.");
            return ("Frame-Height of 0 detected");
        }

        int actMaxRunnerWidth, actMaxRunnerHeight, actLowestYPosition, actHighestYPosition;
        for(VideoSequence vidSequence : videoSequences){
            //detect maximum runner width:
            actMaxRunnerWidth = vidSequence.getMaxRunnerWidth();
            if(actMaxRunnerWidth == -1){
                return ("Detecting max runner width failed. See log for details.");
            }else if(actMaxRunnerWidth == 0){
                Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerDimensions(): Video has no runner (max runner width = 0)");
                return ("Video has no runner (max runner width = 0)");
            }
            if(actMaxRunnerWidth > maxRunnerWidth){
                maxRunnerWidth = actMaxRunnerWidth;
            }

            //detect maximum runner height:
            actMaxRunnerHeight = vidSequence.getMaxRunnerHeight();
            if(actMaxRunnerHeight == -1){
                return ("Detecting max runner height failed. See log for details.");
            }else if(actMaxRunnerHeight == 0){
                Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerDimensions(): Video has no runner (max runner height = 0)");
                return ("Video has no runner (max runner height = 0)");
            }
            if(actMaxRunnerHeight > maxRunnerHeight){
                maxRunnerHeight = actMaxRunnerHeight;
            }

            //detect lowest y-position:
            actLowestYPosition = vidSequence.getLowestYPosition();
            if(actLowestYPosition == -1){
                return ("Detecting max runner width failed. See log for details.");
            }else if(actLowestYPosition == 0){
                Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerDimensions(): All SingleFrames have Runner-Y-Position = 0");
                return ("All SingleFrames have Runner-Y-Position = 0");
            }
            if(actLowestYPosition < lowestYPosition){
                lowestYPosition = actLowestYPosition;
            }

            //detect highest y-position:
            actHighestYPosition = vidSequence.getHighestYPosition();
            if(actHighestYPosition == -1){
                return ("Detecting max runner width failed. See log for details.");
            }else if(actHighestYPosition == 0){
                Log.d("RunalyzerDEBUG", "Runalyzer: detectRunnerDimensions(): All SingleFrames have Runner-Y-Position = 0");
                return ("All SingleFrames have Runner-Y-Position = 0");
            }
            if(actHighestYPosition > highestYPosition){
                highestYPosition = actHighestYPosition;
            }
        }

        middleYPosition = (highestYPosition + lowestYPosition) / 2;

        return "success";
    }

    public String cropSingleFrames(){
        String retval = null;
        if(maxRunnerWidth == 0 || maxRunnerHeight == 0){
            Log.d("RunalyzerDEBUG", "Runalyzer: cropSingleFrames(): max runner width or height is 0");
            return ("Maximum runner width or height is 0");
        }

        int croppingWidth = maxRunnerWidth*2;
        int croppingHeight = maxRunnerHeight*2;
        if (croppingWidth < 100) {
            croppingWidth = 100;
        }
        if (croppingHeight < 100) {
            croppingHeight = 100;
        }

        finalVideo = new FinalVideo(croppingWidth, croppingHeight);

        if(videoSequences.isEmpty()){
            Log.d("RunalyzerDEBUG", "Runalyzer: cropSingleFrames(): No video sequences");
            return ("No video sequences to crop single frames.");
        }
        for(VideoSequence vidSequence : videoSequences){
            retval = vidSequence.cropFrames(croppingWidth, croppingHeight, middleYPosition);
            if(!Objects.equals(retval, "success")){
                return retval;
            }
        }
        return "success";
    }

    public String createFinalVideo(){
        String retval = null;
        if(finalVideo.getWidth() == 0 || finalVideo.getHeight() == 0){
            Log.d("RunalyzerDEBUG", "Runalyzer: createFinalVideo(): cropping width or height is 0");
            return ("Cropping width or height is 0, final video can't be created.");
        }

        if(videoSequences.isEmpty()){
            Log.d("RunalyzerDEBUG", "Runalyzer: createFinalVideo(): No video sequences");
            return ("No video sequences to create final video.");
        }
        retval = finalVideo.setFinalFrames(videoSequences);
        if(!Objects.equals(retval, "success")){
            return retval;
        }

        retval = finalVideo.create();
        if(!Objects.equals(retval, "success")){
            return retval;
        }

        return "success";
    }
}
