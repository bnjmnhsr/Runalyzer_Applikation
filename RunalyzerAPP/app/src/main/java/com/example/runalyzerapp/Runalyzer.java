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
    private File[] inputVideoFiles;
    private List<Uri> inputVideoUris; //added for testing URI
    private List<VideoSequence> videoSequences;
    private int[] millisCreationTime;
    private int maxRunnerWidth;
    private int maxRunnerHeight;
    private int croppingWidth;
    private int croppingHeight;

    public Runalyzer(){
        this.maxRunnerWidth = 0;
        this.maxRunnerHeight = 0;
    }

    public void loadVideoFiles(Context context){
        //Seems like (not 100% sure) android does not allow to access files directly, 11+ introduced scoped storage...
        //that's why i am trying to use URIs instead of files

        //inputVideoFiles = new File(filePath).listFiles();

        inputVideoUris = getVideoUris(context);

        if (inputVideoUris != null) {
            for (Uri uri : inputVideoUris) {
                Log.d("Runalyzer", "URI: " + uri.toString());
            }
        }

        //TODO: get millisCreationTime in another way
        millisCreationTime = new int[inputVideoUris.size()];
        millisCreationTime[0] = 0;      //vid01
        millisCreationTime[1] = 4087;   //vid02
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
        Log.d("Benni", "Detecting Max Runner Width and Height");
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
        croppingWidth = maxRunnerWidth + 10;
        croppingHeight = maxRunnerHeight + 10;
        //added this code because i wanted to test the creation of videos from frames
        //croppingWidth = 1920;
        //croppingHeight = 1080;
        for(VideoSequence vidSequence : videoSequences){
            vidSequence.cropFrames(croppingWidth, croppingHeight);
        }
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
