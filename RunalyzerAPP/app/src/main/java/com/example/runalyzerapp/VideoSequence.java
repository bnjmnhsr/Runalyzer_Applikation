package com.example.runalyzerapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public String separateToFrames(Context context){
        String retval = null;
        VideoFrameProcessor videoFrames = new VideoFrameProcessor();
        retval = videoFrames.videoToFrames(context, videoFilePath, relativeCreationTime);
        if(!Objects.equals(retval, "success")){
            return retval;
        }
        //videoFrames.extractAllFrames(context, videoUri, relativeCreationTime);

        singleFrames = videoFrames.getFrameList();
        if(singleFrames.isEmpty()){
            Log.d("Benni","VideoSequence: separateToFrames(): Empty list of single frames");
            return("Empty list of single frames.");
        }
        return retval;
    }

    public String detectRunnerInformation(){
        String retval = null;
        boolean anyFrameHasRunner = false;
        if(singleFrames.isEmpty()){
            Log.d("Benni","VideoSequence: detectRunnerInformation(): List singleFrames is empty");
            return ("No single frames available");
        }
        Mat backgroundFrame = singleFrames.get(0).getFrame();
        for(SingleFrame frame : singleFrames){
            retval = frame.detectRunnerInformation(backgroundFrame);
            if(!Objects.equals(retval, "success")) {
                return retval;
            }
            if(frame.hasRunner()){
                anyFrameHasRunner = true;
            }
        }
        if(!anyFrameHasRunner){
            Log.d("Benni","VideoSequence: detectRunnerInformation(): No runner in video detected");
            return ("No runner in video detected.");
        }
        return retval;
    }

    public int getMaxRunnerWidth(){
        int maxRunnerWidth = 0;
        if(singleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMaxRunnerWidth(): No single frames");
            return -1;
        }
        for(SingleFrame frame : singleFrames){
            if(frame.getRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = frame.getRunnerWidth();
            }
        }
        return maxRunnerWidth;
    }

    public int getMaxRunnerHeight(){
        int maxRunnerHeight = 0;
        if(singleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMaxRunnerHeight(): No single frames");
            return -1;
        }
        for(SingleFrame frame : singleFrames){
            if(frame.getRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = frame.getRunnerHeight();
            }
        }
        return maxRunnerHeight;
    }

    public String cropFrames(int width, int height){
        String retval = null;
        if(width == 0 || height == 0){
            Log.d("Benni", "VideoSequence: cropFrames(): Width or Height is 0");
            return ("Width or Height is 0, frames can't be cropped.");
        }
        if(singleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: cropFrames(): No single frames");
            return ("No single frames, frames can't be cropped.");
        }
        for(SingleFrame frame : singleFrames){
            if(frame.hasRunner()){
                retval = frame.cropFrame(width, height);
                if(!Objects.equals(retval, "success")){
                    return retval;
                }
            }
        }
        return "success";
    }

    //that was an idea, if we can still work with real paths we can use this to find it...
    public String getRealPathFromURI(Context context, Uri contentUri) {
        if(contentUri == null){
            Log.d("Benni","VideoSequence: getRealPathFromURI(): contentUri == null");
            return ("");
        }
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
