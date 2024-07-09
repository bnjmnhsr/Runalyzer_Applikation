package com.example.runalyzerapp;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Runalyzer {
    private List<Uri> inputVideoUris;
    private List<VideoSequence> videoSequences;
    private int[] millisCreationTime;
    private int maxRunnerWidth;
    private int maxRunnerHeight;
    private int croppingWidth;
    private int croppingHeight;

    public Runalyzer(List<Uri> inputVideoUris, int[] millisCreationTime){
        this.maxRunnerWidth = 0;
        this.maxRunnerHeight = 0;
        this.inputVideoUris = inputVideoUris;
        this.millisCreationTime = millisCreationTime;
    }

    public void detectRunnerInformation(Context context){
        videoSequences = new ArrayList<>();
        for (int i = 0; i < inputVideoUris.size(); i++) {
            Uri videoUri = inputVideoUris.get(i);
            videoSequences.add(new VideoSequence(context, videoUri, millisCreationTime[i]));
            videoSequences.get(i).separateToFrames(context);
            videoSequences.get(i).detectRunnerInformation();
        }
    }

    public void detectMaxRunnerWidthHeight(){
        for(VideoSequence vidSequence : videoSequences){
            if(vidSequence.getMaxRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = vidSequence.getMaxRunnerWidth();
            }
            if(vidSequence.getMaxRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = vidSequence.getMaxRunnerHeight();
            }
        }
        Log.d("Benni", "MaxRunnerWidth: " + maxRunnerWidth );
        Log.d("Benni", "MaxRunnerHeight: " + maxRunnerHeight );
    }

    public void cropSingleFrames(){
        Log.d("Benni", "Cropping Single Frames");
        if (croppingWidth < 100) {
            croppingWidth = 100;
            croppingHeight = maxRunnerHeight + 10;
        } else {
            croppingWidth = maxRunnerWidth + 10;
            croppingHeight = maxRunnerHeight + 10;
        }

        // Ensure the cropping dimensions are even
        croppingWidth = croppingWidth % 2 == 0 ? croppingWidth : croppingWidth - 1;
        croppingHeight = croppingHeight % 2 == 0 ? croppingHeight : croppingHeight - 1;

        Log.d("Benni", "Max Cropping Width: " + croppingWidth + " Max Cropping Height: " + croppingHeight);

        for(VideoSequence vidSequence : videoSequences){
            vidSequence.cropFrames(croppingWidth, croppingHeight);
        }
        Log.d("Benni", "Cropping Single Frames finished");
    }

    public void createFinalVideo(){
        Log.d("Benni", "Creating Final Video");
        VideoCompilator videoCompilator = new VideoCompilator(croppingWidth, croppingHeight);
        videoCompilator.selectFinalFrames(videoSequences);
        videoCompilator.createFinalVideo();
    }


    //TODO: This only works because there are only two videos in the camera folder, so it just finds both of them
    //TODO: For real situation we need to do some adjustments
    public List<Uri> getVideoUris(Context context) {
        List<Uri> videoUris = new ArrayList<>();
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.RELATIVE_PATH};
        String selection = MediaStore.Video.Media.RELATIVE_PATH + "=?";
        String[] selectionArgs = new String[]{ "Video_Files" }; // Replace with your folder


        try (Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        )) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String name = cursor.getString(nameColumn);
                    String relativePath = cursor.getString(pathColumn);
                    Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    videoUris.add(contentUri);
                    Log.d("VideoQuery", "Video found: " + name + " in path: " + relativePath + " with URI: " + contentUri.toString());
                }
            }
        }
        return videoUris;
    }
}
