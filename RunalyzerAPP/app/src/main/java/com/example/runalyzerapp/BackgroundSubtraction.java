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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class BackgroundSubtraction implements RunnerDetection {
    private static final List<Integer> timecodes = Arrays.asList(
            61586387, 61591616, 61586687, 61591949, 61587087, 61592282,
            61587520, 61592649, 61587854, 61592982, 61588187, 61593316,
            61588520, 61593649, 61588854, 61593982, 61589187, 61594316,
            61589520, 61594649, 61589854, 61594982, 61590220, 61595316,
            61590554, 61595682, 61590920, 61595916, 61591320, 61596149
    );
    @Override
    public RunnerInformation detectRunnerInformation(SingleFrame singleFrame, Context context) {
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
        //TODO: check what's a correct value to identify a runner (depends also on camera-distance)
        if(moments.get_m00() > 0){
            singleFrame.setHasRunner(true);
            int centerX = (int) (moments.get_m10() / moments.get_m00());
            int centerY = (int) (moments.get_m01() / moments.get_m00());

            Rect boundingRect = Imgproc.boundingRect(differenceImg);
            if (timecodes.contains((int)singleFrame.getTimecode())) {
                String filename = Integer.toString((int)singleFrame.getTimecode());
                Imgproc.cvtColor(differenceImg, differenceImg, Imgproc.COLOR_GRAY2RGB);
                Imgproc.rectangle(differenceImg, boundingRect, new Scalar(0, 255, 0));
                Imgproc.drawMarker(differenceImg, new Point(centerX, centerY), new Scalar(255, 0, 0), Imgproc.MARKER_CROSS, Imgproc.LINE_8, 1);
                saveMatToGallery(context, differenceImg, filename);
            }
//            String filename = Integer.toString((int)singleFrame.getTimecode());
//            Imgproc.cvtColor(differenceImg, differenceImg, Imgproc.COLOR_GRAY2RGB);
//            Imgproc.rectangle(differenceImg, boundingRect, new Scalar(0, 255, 0));
//            Imgproc.drawMarker(differenceImg, new Point(centerX, centerY), new Scalar(255, 0, 0), Imgproc.MARKER_CROSS, Imgproc.LINE_8, 1);
//            saveMatToGallery(context, differenceImg, filename);

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
        //TODO: make kernel size dependent of Mat size
        try {
            Imgproc.erode(resultImg, resultImg, Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3)));
        } catch (Exception e) {
            Log.e("Benni", "BackgroundSubtraction: subtract(): " + Log.getStackTraceString(e));
            return emptyMat;
        }

        emptyMat.release();
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