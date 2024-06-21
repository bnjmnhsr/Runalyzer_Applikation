package com.example.runalyzerapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class VideoSequence {
    private String videoFilePath;
    private Uri videoUri;
    private int relativeCreationTime;
    private List<SingleFrame> singleFrames = new ArrayList<>();

    public VideoSequence(Context context, Uri videoUri, int relativeCreationTime) {
        this.videoUri = videoUri;
        this.videoFilePath = getRealPathFromURI(context, videoUri);
        this.relativeCreationTime = relativeCreationTime;
    }

    public List<SingleFrame> getSingleFrames(){
        return singleFrames;
    }

    public void separateToFrames(Context context){
        VideoFrameProcessor videoFrames = new VideoFrameProcessor();
        videoFrames.videoToFrames(context, videoFilePath, relativeCreationTime);
        //videoFrames.extractAllFrames(context, videoUri, relativeCreationTime);

        singleFrames = videoFrames.getFrameList();
    }

    public void detectRunnerInformation(){
        Log.d("Benni", "Start: Detecting Runner Information");
        Mat backgroundFrame = singleFrames.get(0).getFrame();
        int index = 0;
        for(SingleFrame frame : singleFrames){
            Log.d("Benni", "Detecting Runner Information; Start Frame: " + (index++));
            frame.detectRunnerInformation(backgroundFrame);
        }
        Log.d("Benni", "Finished: Detecting Runner Information");
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

    //that was an idea, if we can still work with real paths we can use this to find it...
    public String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
    }
}
