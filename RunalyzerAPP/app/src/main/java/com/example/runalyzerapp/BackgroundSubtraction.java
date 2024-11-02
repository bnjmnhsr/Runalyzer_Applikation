package com.example.runalyzerapp;

import android.util.Log;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class BackgroundSubtraction implements RunnerDetection {

    @Override
    public RunnerInformation detectRunnerInformation(SingleFrame singleFrame) {
        RunnerInformation runnerInformation;

        if(singleFrame.getPreviousFrame() == null){
            //this is the first frame in the video, should have no runner
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

        //detect runner information
        Moments moments;
        try {
            moments = Imgproc.moments(differenceImg);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: detectRunnerInformation(): " + Log.getStackTraceString(e));
            return null;
        }

        if(moments.get_m00() > 0){
            singleFrame.setHasRunner(true);
            int centerX = (int) (moments.get_m10() / moments.get_m00());
            int centerY = (int) (moments.get_m01() / moments.get_m00());

            Rect boundingRect = Imgproc.boundingRect(differenceImg);
            differenceImg.release();
            runnerInformation = new RunnerInformation(new Vector(centerX, centerY), boundingRect.width, boundingRect.height);
        }
        else{
            singleFrame.setHasRunner(false);
            differenceImg.release();
            runnerInformation = new RunnerInformation();
        }

        return runnerInformation;
    }

    private Mat subtract(Mat background, Mat img) {
        Mat emptyMat = new Mat();
        if(background.empty() || img.empty()){
            return emptyMat;
        }

        //do subtraction
        Mat resultImg1 = new Mat();
        Mat resultImg2 = new Mat();
        Mat resultImg = new Mat();
        try {
            Core.subtract(img, background, resultImg1);
            Core.subtract(background, img, resultImg2);
            Core.add(resultImg1, resultImg2, resultImg);
            resultImg1.release();
            resultImg2.release();
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        //binarize colored difference-img
        int thresholdValue = 65; // 1-255
        int thresholdType = 0; // 0: Binary, 1: Binary Inverted, 2: Truncate, 3: To Zero, 4: To Zero Inverted
        try {
            Imgproc.threshold(resultImg, resultImg, thresholdValue, 255, thresholdType);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        //grayscale colored binarized-image
        try {
            Imgproc.cvtColor(resultImg, resultImg, Imgproc.COLOR_RGB2GRAY);
        }catch(Exception e){
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        //binarize
        thresholdValue = 1; // 1-255
        thresholdType = 0; // 0: Binary, 1: Binary Inverted, 2: Truncate, 3: To Zero, 4: To Zero Inverted
        try {
            Imgproc.threshold(resultImg, resultImg, thresholdValue, 255, thresholdType);
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        //remove noise
        try {
            Imgproc.erode(resultImg, resultImg, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3)));
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        emptyMat.release();
        return resultImg;
    }
}