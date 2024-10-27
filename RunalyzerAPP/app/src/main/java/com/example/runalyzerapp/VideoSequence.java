package com.example.runalyzerapp;

import static org.opencv.videoio.Videoio.CAP_PROP_FPS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VideoSequence {
    private String videoFilePath;
    private Uri videoUri;
    private int relativeCreationTime;
    private int videoDurationInMillis;
    private List<SingleFrame> selectedSingleFrames = new ArrayList<>();
    private RunnerDetection runnerDetector;

    public VideoSequence(Context context, Uri videoUri, int relativeCreationTime) {
        this.videoUri = videoUri;
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

    public String separateToFrames(Context context, int totalMillisAllVideos){
        if(Objects.equals(videoFilePath, "")){
            Log.d("Benni","VideoSequence: separateToFrames(): Empty videoFilePath");
            return("Empty video file path. Probably no Uri detected.");
        }
        VideoCapture videoCapture = null;
        try {
            videoCapture = new VideoCapture(videoFilePath);
        } catch (Exception e) {
            Log.e("Benni", Log.getStackTraceString(e));
            return Log.getStackTraceString(e);
        }

        if (!videoCapture.isOpened()) {
            Log.d("Benni", "VideoSequence: separateToFrames(): Failed to open video file: " + videoFilePath);
            return ("Failed to open video file: " + videoFilePath);
        }

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double fps = videoCapture.get(CAP_PROP_FPS);
        if(fps == 0){
            Log.d("Benni","VideoSequence: separateToFrames(): fps can't be read from input video");
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
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();

            frame.copyTo(currentFrame); //TODO: maybe remove
            int newMatWidth = (int)(Math.sqrt(reductionFactor) * currentFrame.width());
            int newMatHeight = (int)(newMatWidth * ((double)currentFrame.height()/(double)currentFrame.width()));
            //würde einfach mehrere timecodes einfügen mit unterschiedlich großen Kreisen, dann kann man in einem Testing alles für das Video Testen
            //position at x=1000 y=400
            int x = 1000;
            int y = 400;
            switch ((int)timecode) {
                case 61586387:
                case 61591616:
                    Log.d("Benni", "Detected Frame");
                    currentFrame.put(x, y, new double[]{255, 255, 255});
                    break;
                case 61586687:
                case 61591949:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 2, context, (int)timecode);
                    break;
                case 61587087:
                case 61592282:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 3, context, (int)timecode);
                    break;
                case 61587520:
                case 61592649:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 4, context, (int)timecode);
                    break;
                case 61587854:
                case 61592982:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 5, context, (int)timecode);
                    break;
                case 61588187:
                case 61593316:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 6, context, (int)timecode);
                    break;
                case 61588520:
                case 61593649:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 7, context, (int)timecode);
                    break;
                case 61588854:
                case 61593982:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 8, context, (int)timecode);
                    break;
                case 61589187:
                case 61594316:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 9, context, (int)timecode);
                    break;
                case 61589520:
                case 61594649:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 10, context, (int)timecode);
                    break;
                case 61589854:
                case 61594982:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 11, context, (int)timecode);
                    break;
                case 61590220:
                case 61595316:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 12, context, (int)timecode);
                    break;
                case 61590554:
                case 61595682:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 13, context, (int)timecode);
                    break;
                case 61590920:
                case 61595916:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 14, context, (int)timecode);
                    break;
                case 61591320:
                case 61596149:
                    Log.d("Benni", "Detected Frame");
                    currentFrame = addCircleToMat(currentFrame, x, y, 15, context, (int)timecode);
                    break;
            }
            Imgproc.resize(currentFrame, currentFrame, new Size(newMatWidth,newMatHeight));

            SingleFrame singleFrame = new SingleFrame(currentFrame, previousFrame, timecode);
            singleFrame.setRunnerInformation(runnerDetector.detectRunnerInformation(singleFrame, context));

            if(singleFrame.hasRunner()){
                selectedSingleFrames.add(singleFrame);
            }

            timecode += millisBetweenFrames;
            previousFrame = currentFrame;
        }

        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni","VideoSequence: separateToFrames(): frameList still empty, videoCapture failed");
            return("No single frame could be captured from input Video.");
        }

        //remove frames with outliner values for width, height and centerY to equalize e.g. shaking camera or focus change
        selectedSingleFrames.removeAll(checkIfFramesCanBeRemoved(selectedSingleFrames));

        frame.release();
        videoCapture.release();

        return ("success");
    }

    private Mat addCircleToMat(Mat mat, int x, int y, int radius, Context context, int timecode){
        Imgproc.circle(mat, new org.opencv.core.Point(x,y), radius, new org.opencv.core.Scalar(255,255,255), -1);
        saveMatToGallery(context, mat, Integer.toString(timecode) + "_with_noise.jpg");
        return mat;
    }

    public int getMaxRunnerWidth(){
        int maxRunnerWidth = 0;
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMaxRunnerWidth(): No single frames");
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
            Log.d("Benni", "VideoSequence: getMaxRunnerHeight(): No single frames");
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
            Log.d("Benni", "VideoSequence: getMiddleYPosition(): No single frames.");
            return -1;
        }

        int actYPos;
        int lowestYPosition = selectedSingleFrames.get(0).getFrame().height();
        if(lowestYPosition == 0){
            Log.d("Benni", "VideoSequence: getMiddleYPosition(): Frame-Height of 0 detected, error.");
            return -1;
        }

        for(SingleFrame frame : selectedSingleFrames){
            actYPos = frame.getRunnerInformation().getRunnerPosition().getY();
            if(actYPos == 0){
                Log.d("Benni", "VideoSequence: getMiddleYPosition(): SingleFrame with Runner-Y-Position = 0 detected.");
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
            Log.d("Benni", "VideoSequence: getMiddleYPosition(): No single frames.");
            return -1;
        }

        int actYPos;
        int highestYPosition = 0;

        for(SingleFrame frame : selectedSingleFrames){
            actYPos = frame.getRunnerInformation().getRunnerPosition().getY();
            if(actYPos == 0){
                Log.d("Benni", "VideoSequence: getMiddleYPosition(): SingleFrame with Runner-Y-Position = 0 detected.");
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
            Log.d("Benni", "VideoSequence: smoothXPos(): No single frames");
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
            Log.d("Benni", "VideoSequence: cropFrames(): Width or Height is 0");
            return ("Width or Height is 0, frames can't be cropped.");
        }
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: cropFrames(): No single frames");
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

    public int getVideoDurationFromURI(Context context, Uri contentUri) {
        if(contentUri == null){
            Log.d("Benni","VideoSequence: getVideoDurationFromURI(): contentUri == null");
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
                    Log.d("Benni", "removed " + (int)fr.getTimecode() + " because width= " + fr.getRunnerInformation().getRunnerWidth() + " > upperBound= " + upperBound_runnerWidths);
                }
            }
            if(fr.getRunnerInformation().getRunnerHeight() > upperBound_runnerHeights){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                    Log.d("Benni", "removed " + (int)fr.getTimecode() + " because height= " + fr.getRunnerInformation().getRunnerHeight() + " > upperBound= " + upperBound_runnerHeights);
                }
            }
            if(fr.getRunnerInformation().getRunnerPosition().getY() > upperBound_centerY){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                    Log.d("Benni", "removed " + (int)fr.getTimecode() + " because centerY= " + fr.getRunnerInformation().getRunnerPosition().getY() + " > upperBound= " + upperBound_centerY);
                }
            }
            if(fr.getRunnerInformation().getRunnerPosition().getY() < lowerBound_centerY){
                if(!framesToRemove.contains(fr)){
                    framesToRemove.add(fr);
                    Log.d("Benni", "removed " + (int)fr.getTimecode() + " because centerY= " + fr.getRunnerInformation().getRunnerPosition().getY() + " < lowerBound= " + lowerBound_centerY);
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

    public void saveMatToGallery(Context context, Mat mat, String filename) {
        Bitmap img = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, img);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri imageUri = context.getContentResolver().insert(externalContentUri, values);

        try {
            if (imageUri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri);
                if (outputStream != null) {
                    img.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                } else {
                    Log.e("Benni", "OutputStream is null.");
                }
            } else {
                Log.e("Benni", "ImageUri is null.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Benni", Log.getStackTraceString(e));
        }
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
