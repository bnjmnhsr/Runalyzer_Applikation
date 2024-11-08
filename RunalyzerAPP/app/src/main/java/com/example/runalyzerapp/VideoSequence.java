package com.example.runalyzerapp;

import static org.opencv.videoio.Videoio.CAP_PROP_FPS;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VideoSequence {
    private String videoFilePath;
    private int relativeCreationTime;
    private int videoDurationInMillis;
    private List<SingleFrame> selectedSingleFrames = new ArrayList<>();
    private RunnerDetection runnerDetector;

    public VideoSequence(Context context, Uri videoUri, int relativeCreationTime) {
        this.videoFilePath = getRealPathFromURI(context, videoUri);
        this.relativeCreationTime = relativeCreationTime;
        this.videoDurationInMillis = getVideoDurationFromURI(context, videoUri);
    }

    public void setDetectionMethod(RunnerDetection detectionMethod){
        this.runnerDetector = detectionMethod;
    }

    public List<SingleFrame> getSelectedSingleFrames(){
        return selectedSingleFrames;
    }

    public int getVideoDurationInMillis(){
        return videoDurationInMillis;
    }

    public String separateToFrames(int totalMillisAllVideos){
        if(Objects.equals(videoFilePath, "")){
            Log.d("RunalyzerDEBUG","VideoSequence: separateToFrames(): Empty videoFilePath");
            return("Empty video file path. Probably no Uri detected.");
        }
        VideoCapture videoCapture = null;
        try {
            videoCapture = new VideoCapture(videoFilePath);
        } catch (Exception e) {
            Log.e("RunalyzerDEBUG", Log.getStackTraceString(e));
            return Log.getStackTraceString(e);
        }

        if (!videoCapture.isOpened()) {
            Log.d("RunalyzerDEBUG", "VideoSequence: separateToFrames(): Failed to open video file: " + videoFilePath);
            return ("Failed to open video file: " + videoFilePath);
        }

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double fps = videoCapture.get(CAP_PROP_FPS);
        if(fps == 0){
            Log.d("RunalyzerDEBUG","VideoSequence: separateToFrames(): fps can't be read from input video");
            return("fps can't be read from input video");
        }
        double millisBetweenFrames = (1000.0/fps);

        //calculate most efficient Mat size:
        long totalFrameCountAllVideos = (int)(totalMillisAllVideos/1000.0 * fps) + 1;
        long sizeOfOneMatInByte = 1920*1080*3; //assume CV_8UC3-format (8 bit per channel, 3 channels(RGB))
        long totalMatSize = sizeOfOneMatInByte * totalFrameCountAllVideos;
        double reductionFactor = 1500000000.0 / totalMatSize; //use 1.5 GB memory for Mats

        Mat previousFrame = null;

        while (videoCapture.read(frame)) {
            Mat currentFrame = new Mat();

            frame.copyTo(currentFrame);
            int newMatWidth = (int)(Math.sqrt(reductionFactor) * currentFrame.width());
            int newMatHeight = (int)(newMatWidth * ((double)currentFrame.height()/(double)currentFrame.width()));
            Imgproc.resize(currentFrame, currentFrame, new Size(newMatWidth,newMatHeight));

            SingleFrame singleFrame = new SingleFrame(currentFrame, previousFrame, timecode);
            singleFrame.setRunnerInformation(runnerDetector.detectRunnerInformation(singleFrame));

            if(singleFrame.hasRunner()){
                selectedSingleFrames.add(singleFrame);
            }

            timecode += millisBetweenFrames;
            previousFrame = currentFrame;
        }

        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG","VideoSequence: separateToFrames(): frameList still empty, videoCapture failed");
            return("No single frame could be captured from input Video.");
        }

        //remove frames with outliner values for width, height and centerY to equalize e.g. shaking camera or focus change
        selectedSingleFrames.removeAll(checkIfFramesCanBeRemoved(selectedSingleFrames));

        frame.release();
        videoCapture.release();

        return ("success");
    }

    public int getMaxRunnerWidth(){
        int maxRunnerWidth = 0;
        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG", "VideoSequence: getMaxRunnerWidth(): No single frames");
            return -1;
        }
        for(SingleFrame frame : selectedSingleFrames){
            if(frame.getRunnerInformation().getRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = frame.getRunnerInformation().getRunnerWidth();
            }
        }
        return maxRunnerWidth;
    }

    public int getMaxRunnerHeight(){
        int maxRunnerHeight = 0;
        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG", "VideoSequence: getMaxRunnerHeight(): No single frames");
            return -1;
        }
        for(SingleFrame frame : selectedSingleFrames){
            if(frame.getRunnerInformation().getRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = frame.getRunnerInformation().getRunnerHeight();
            }
        }
        return maxRunnerHeight;
    }

    public int getLowestYPosition(){
        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG", "VideoSequence: getMiddleYPosition(): No single frames.");
            return -1;
        }

        int actYPos;
        int lowestYPosition = selectedSingleFrames.get(0).getFrame().height();
        if(lowestYPosition == 0){
            Log.d("RunalyzerDEBUG", "VideoSequence: getMiddleYPosition(): Frame-Height of 0 detected, error.");
            return -1;
        }

        for(SingleFrame frame : selectedSingleFrames){
            actYPos = frame.getRunnerInformation().getRunnerPosition().getY();
            if(actYPos == 0){
                Log.d("RunalyzerDEBUG", "VideoSequence: getMiddleYPosition(): SingleFrame with Runner-Y-Position = 0 detected.");
                return -1;
            }

            if(actYPos < lowestYPosition){
                lowestYPosition = actYPos;
            }
        }

        return lowestYPosition;
    }

    public int getHighestYPosition(){
        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG", "VideoSequence: getMiddleYPosition(): No single frames.");
            return -1;
        }

        int actYPos;
        int highestYPosition = 0;

        for(SingleFrame frame : selectedSingleFrames){
            actYPos = frame.getRunnerInformation().getRunnerPosition().getY();
            if(actYPos == 0){
                Log.d("RunalyzerDEBUG", "VideoSequence: getMiddleYPosition(): SingleFrame with Runner-Y-Position = 0 detected.");
                return -1;
            }
            if(actYPos > highestYPosition){
                highestYPosition = actYPos;
            }
        }

        return highestYPosition;
    }

    public String smoothXPos(){
        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG", "VideoSequence: smoothXPos(): No single frames");
            return ("No single frames, xPos can't be smoothed.");
        }

        int smoothFactor = 10;   //the lower the smoothFactor, the more erratic, but better matching with original xPos; the higher the smoothFactor, the more stable, but worse matching with original xPos
        int diffXPositions, newXPos;
        float step = 0;
        for(int i = 10; i < selectedSingleFrames.size()-10; i++){      //keep xPos of first and last 10 frames as they are because xPos at edge is not representative
            if(smoothFactor > selectedSingleFrames.size()-20){
                break;
            }
            if(i < selectedSingleFrames.size()-10-smoothFactor+1){      //step needs to be stable for the last frames (count of smoothFactor)
                diffXPositions = selectedSingleFrames.get(i + smoothFactor-1).getRunnerInformation().getRunnerPosition().getX() - selectedSingleFrames.get(i-1).getRunnerInformation().getCorrectedRunnerPosition().getX();
                step = (float) diffXPositions / smoothFactor;
            }
            newXPos = selectedSingleFrames.get(i-1).getRunnerInformation().getCorrectedRunnerPosition().getX() + Math.round(step);
            selectedSingleFrames.get(i).changeXPosition(newXPos);
        }

        return "success";
    }

    public String cropFrames(int width, int height, int yPosition){
        String retval;
        if(width == 0 || height == 0){
            Log.d("RunalyzerDEBUG", "VideoSequence: cropFrames(): Width or Height is 0");
            return ("Width or Height is 0, frames can't be cropped.");
        }
        if(selectedSingleFrames.isEmpty()){
            Log.d("RunalyzerDEBUG", "VideoSequence: cropFrames(): No single frames");
            return ("No single frames, frames can't be cropped.");
        }
        for(SingleFrame frame : selectedSingleFrames){
            if(frame.hasRunner()){
                retval = frame.cropFrame(width, height, yPosition);
                if(!Objects.equals(retval, "success")){
                    return retval;
                }
            }
        }
        return "success";
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        if(contentUri == null){
            Log.d("RunalyzerDEBUG","VideoSequence: getRealPathFromURI(): contentUri == null");
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

    public int getVideoDurationFromURI(Context context, Uri contentUri) {
        if(contentUri == null){
            Log.d("RunalyzerDEBUG","VideoSequence: getVideoDurationFromURI(): contentUri == null");
            return -1;
        }
        String[] proj = { MediaStore.Video.Media.DURATION };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return -1;
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int videoDurationMillis = Integer.parseInt(cursor.getString(column_index));
            cursor.close();
            return videoDurationMillis;
        }
    }

    private List<SingleFrame> checkIfFramesCanBeRemoved(List<SingleFrame> singleframes){
        List<SingleFrame> framesToRemove = new ArrayList<>();

        int selectedFramecount = singleframes.size();
        int[] runnerWidths = new int[selectedFramecount];
        int[] runnerHeights = new int[selectedFramecount];
        int[] centerY = new int[selectedFramecount];
        for(int i = 0; i < selectedFramecount; i++){
            runnerWidths[i] = singleframes.get(i).getRunnerInformation().getRunnerWidth();
            runnerHeights[i] = singleframes.get(i).getRunnerInformation().getRunnerHeight();
            centerY[i] = singleframes.get(i).getRunnerInformation().getRunnerPosition().getY();
        }

        double upperBound_runnerWidths = calculateUpperBound(runnerWidths, 15);
        double upperBound_runnerHeights = calculateUpperBound(runnerHeights, 15);
        double upperBound_centerY = calculateUpperBound(centerY, 5); //start- and end-frames sometimes have higher YPos because feet is detected first/last
        double lowerBound_centerY = calculateLowerBound(centerY, 15);

        for(SingleFrame fr : singleframes){
            if(fr.getRunnerInformation().getRunnerWidth() > upperBound_runnerWidths){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                }
            }
            if(fr.getRunnerInformation().getRunnerHeight() > upperBound_runnerHeights){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                }
            }
            if(fr.getRunnerInformation().getRunnerPosition().getY() > upperBound_centerY){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                }
            }
            if(fr.getRunnerInformation().getRunnerPosition().getY() < lowerBound_centerY){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                }
            }
        }

        return framesToRemove;
    }

    private double calculateUpperBound(int[] data, int percentile){
        // Interquartile Range (IQR) method

        Arrays.sort(data);

        int n = data.length;
        double index =  percentile / 100.0 * (n - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        double Q1;
        if(lower == upper){
            Q1 = data[lower];
        }else{
            Q1 = data[lower] + (index - lower) * (data[upper] - data[lower]);
        }

        index =  (100 - percentile) / 100.0 * (n - 1);
        lower = (int) Math.floor(index);
        upper = (int) Math.ceil(index);
        double Q3;
        if(lower == upper){
            Q3 = data[lower];
        }else{
            Q3 = data[lower] + (index - lower) * (data[upper] - data[lower]);
        }

        double IQR = Q3 - Q1;

        return Q3 + 1.5 * IQR;
    }

    private double calculateLowerBound(int[] data, int percentile){
        // Interquartile Range (IQR) method

        Arrays.sort(data);

        int n = data.length;
        double index =  percentile / 100.0 * (n - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        double Q1;
        if(lower == upper){
            Q1 = data[lower];
        }else{
            Q1 = data[lower] + (index - lower) * (data[upper] - data[lower]);
        }

        index =  (100 - percentile) / 100.0 * (n - 1);
        lower = (int) Math.floor(index);
        upper = (int) Math.ceil(index);
        double Q3;
        if(lower == upper){
            Q3 = data[lower];
        }else{
            Q3 = data[lower] + (index - lower) * (data[upper] - data[lower]);
        }

        double IQR = Q3 - Q1;

        return Q1 - 1.5 * IQR;
    }
}
