package com.example.runalyzerapp;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BackgroundSubtraction implements RunnerDetection {
    @Override
    public RunnerInformation detectRunnerInformation(SingleFrame singleFrame) {
        RunnerInformation runnerInformation;

        if(singleFrame.getPreviousFrame() == null){
            //this is the first frame in the video, should has no runner
            singleFrame.setHasRunner(false);
            return singleFrame.getRunnerInformation();
        }

        //create binary difference image
        Mat differenceImg = subtract(singleFrame.getPreviousFrame(), singleFrame.getFrame());
        if(differenceImg.empty()){
            Log.d("Benni","BackgroundSubtraction: detectRunnerInformation(): Background subtraction failed");
            differenceImg.release();
            return null;
        }



        //find all white-pixel contours
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        try {
            Imgproc.findContours(differenceImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: detectRunnerInformation(): " + Log.getStackTraceString(e));
            return null;
        }
        hierarchy.release(); //we don't need hierarchy
        differenceImg.release();

        if(contours.isEmpty()){
            singleFrame.setHasRunner(false);
            runnerInformation = new RunnerInformation();
            return runnerInformation;
        }

        //detect biggest contour --> should be runner
        double areaOfActualContour = 0;
        double areaOfBiggestContour = 0;
        int indexOfBiggestContour = 0;
        for (int i = 0; i < contours.size(); ++i) {
            try {
                areaOfActualContour = Imgproc.contourArea(contours.get(i));
            } catch (Exception e) {
                Log.e("Benni", "BackgroundSubtraction: detectRunnerInformation(): " + Log.getStackTraceString(e));
                return null;
            }
            if(areaOfActualContour > areaOfBiggestContour){
                areaOfBiggestContour = areaOfActualContour;
                indexOfBiggestContour = i;
            }
        }

        //detect runner information
        Moments moments;
        try {
            moments = Imgproc.moments(contours.get(indexOfBiggestContour));
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: detectRunnerInformation(): " + Log.getStackTraceString(e));
            return null;
        }
        //TODO: check what's a correct value to identify a runner (depends also on camera-distance)
        double test_variable = moments.get_m00();
        if(moments.get_m00() > 200){
            singleFrame.setHasRunner(true);
            int centerX = (int) (moments.get_m10() / moments.get_m00());
            int centerY = (int) (moments.get_m01() / moments.get_m00());
            int runnerWidth = contours.get(indexOfBiggestContour).width();
            int runnerHeight = contours.get(indexOfBiggestContour).height();

            runnerInformation = new RunnerInformation(new Vector(centerX, centerY), runnerWidth, runnerHeight);
        }
        else{
            singleFrame.setHasRunner(false);
            runnerInformation = new RunnerInformation();
        }

        for (MatOfPoint contour : contours) {
            contour.release();
        }

        return runnerInformation;
    }

    public Mat subtract(Mat background, Mat img) {
        Mat emptyMat = new Mat();
        if(background.empty() || img.empty()){
            return emptyMat;
        }
        Mat backgroundGray = new Mat();
        Mat imgGray = new Mat();
        try {
            Imgproc.cvtColor(background, backgroundGray, Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);
        }catch(Exception e){
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        Mat backgroundGrayBlurred = new Mat();
        Mat imgGrayBlurred = new Mat();
        try {
            Imgproc.medianBlur(backgroundGray, backgroundGrayBlurred, 3);
            Imgproc.medianBlur(imgGray, imgGrayBlurred, 3);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        if (false) { //TODO: add requirement for inverting (maybe black shirt)
            try {
                Core.bitwise_not(backgroundGrayBlurred, backgroundGrayBlurred);
                Core.bitwise_not(imgGrayBlurred, imgGrayBlurred);
            } catch (Exception e) {
                Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
                return emptyMat;
            }
        }

        Mat resultImg = new Mat();
        try {
            Core.subtract(imgGrayBlurred, backgroundGrayBlurred, resultImg);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }
        try {
            Imgproc.medianBlur(resultImg, resultImg, 3); //davor 7
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        int thresholdValue = 35; // 0-255, was 35 before
        int thresholdType = 0; // 0: Binary, 1: Binary Inverted, 2: Truncate, 3: To Zero, 4: To Zero Inverted
        try {
            Imgproc.threshold(resultImg, resultImg, thresholdValue, 255, thresholdType);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        emptyMat.release();
        backgroundGray.release();
        imgGray.release();
        backgroundGrayBlurred.release();
        imgGrayBlurred.release();

        return resultImg;
    }

    //TODO: only for debugging, remove
    //Example-invoke: saveMatToGallery(context, differenceImg, Integer.toString(newTimecode));
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
}