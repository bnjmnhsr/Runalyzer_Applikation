package com.example.runalyzerapp;

import android.util.Log;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class BackgroundSubtraction {
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
}