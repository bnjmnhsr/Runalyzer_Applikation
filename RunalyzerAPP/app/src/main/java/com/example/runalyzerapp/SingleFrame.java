package com.example.runalyzerapp;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class SingleFrame {
    private Mat frame;
    private Mat croppedFrame = null;
    private boolean hasRunner;
    private Vector runnerPosition;
    private int runnerWidth;
    private int runnerHeight;
    private double timecode;

    public SingleFrame(){
        frame = new Mat();
        hasRunner = false;
        runnerPosition = new Vector();
        runnerWidth = 0;
        runnerHeight = 0;
        timecode = 0;
    }

    public SingleFrame(Mat frame, double timecode){
        this.frame = frame;
        hasRunner = false;
        runnerPosition = new Vector();
        runnerWidth = 0;
        runnerHeight = 0;
        this.timecode = timecode;
    }

    public Mat getFrame(){
        return frame;
    }

    public Mat getCroppedFrame(){
        return croppedFrame;
    }

    public boolean hasRunner(){
        return hasRunner;
    }

    public Vector getRunnerPosition(){
        return runnerPosition;
    }

    public double getTimecode(){
        return timecode;
    }

    public String detectRunnerInformation(Mat backgroundFrame){
        String retval = null;
        if(backgroundFrame.empty()){
            Log.d("Benni","SingleFrame: detectRunnerInformation(): backgroundFrame is empty");
            return ("Background frame is empty.");
        }
        BackgroundSubtraction backgroundSubtractor = new BackgroundSubtraction();
        Mat differenceImg = backgroundSubtractor.subtract(backgroundFrame, frame);
        if(differenceImg.empty()){
            Log.d("Benni","SingleFrame: detectRunnerInformation(): Background subtraction failed");
            return ("Background subtraction failed. See log for details.");
        }

        //filter the image to remove noise
        ObjectDetection objDetector = new ObjectDetection();
        differenceImg = objDetector.removeNoise(differenceImg);
        if(differenceImg.empty()){
            Log.d("Benni","SingleFrame: detectRunnerInformation(): Object detection failed");
            return ("Object detection failed. See log for details.");
        }

        Moments moments = Imgproc.moments(differenceImg);
        //get_m00 counts number of white pixels in the image, if enough pixels counted there exists a runner...
        //TODO: check what's a correct value to identify a runner (depends also on camera-distance)
        if(moments.get_m00() > 50000 && moments.get_m00() < 120000){
            hasRunner = true;
            runnerPosition.setX((int) (moments.get_m10() / moments.get_m00()));
            runnerPosition.setY((int) (moments.get_m01() / moments.get_m00()));
            runnerWidth = objDetector.getObjectWidth();
            runnerHeight = objDetector.getObjectHeight();
        }
        else{
            hasRunner = false;
            runnerWidth = 0;
            runnerHeight = 0;
        }

        differenceImg.release();
        return ("success");
    }

    public int getRunnerWidth(){ return runnerWidth; }

    public int getRunnerHeight(){
        return runnerHeight;
    }

    public String cropFrame(int width, int height){
        if(width == 0 || height == 0){
            Log.d("Benni", "SingleFrame: cropFrame(): Width or Height is 0");
            return ("Width or Height is 0, frame can't be cropped.");
        }
        if(hasRunner){
            if(runnerPosition.getX() == 0|| runnerPosition.getY() == 0){
                Log.d("Benni", "SingleFrame: cropFrame(): No runner position available");
                return ("No runner position available, frame can't be cropped.");
            }
            int xStartPos = runnerPosition.getX()-(width/2);
            int yStartPos = runnerPosition.getY()-(height/2);

            //TODO: find solution to crop Frame when Runner is at the edge
            if(frame.width() == 0|| frame.height() == 0){
                Log.d("Benni", "SingleFrame: cropFrame(): Frame size is 0");
                return ("No frame size available, frame can't be cropped.");
            }
            if(xStartPos > 0 && xStartPos < (frame.width()-width)){
                if(yStartPos > 0 && yStartPos < (frame.height()-height)){
                    Rect rectCrop = new Rect(xStartPos, yStartPos, width, height);
                    croppedFrame = new Mat(frame,rectCrop);
                }
            }
        }
        return "success";
    }
}
