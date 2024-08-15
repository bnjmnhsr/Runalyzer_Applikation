package com.example.runalyzerapp;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.io.OutputStream;

public class SingleFrame {
    private Mat frame;
    private Mat previousFrame;
    private Mat croppedFrame = null;
    private boolean hasRunner;
    private double timecode;
    private RunnerInformation runnerInformation;

    public SingleFrame(Mat frame, Mat previousFrame, double timecode){
        this.frame = frame;
        this.previousFrame = previousFrame;
        this.timecode = timecode;
        hasRunner = false;
        runnerInformation = new RunnerInformation();
    }

    public Mat getFrame(){
        return frame;
    }

    public Mat getPreviousFrame(){
        return previousFrame;
    }

    public Mat getCroppedFrame(){
        return croppedFrame;
    }

    public boolean hasRunner(){
        return hasRunner;
    }

    public void setHasRunner(boolean hasRunner){
        this.hasRunner = hasRunner;
        if(!hasRunner){
            runnerInformation = new RunnerInformation();
            runnerInformation.setEmptyRunnerInformation();
        }
    }

    public RunnerInformation getRunnerInformation(){
        return runnerInformation;
    }

    public void setRunnerInformation(RunnerInformation runnerInformation){
        this.runnerInformation = runnerInformation;
    }

    public double getTimecode(){
        return timecode;
    }

    public String cropFrame(int width, int height){
        if(width == 0 || height == 0){
            Log.d("Benni", "SingleFrame: cropFrame(): Width or Height is 0");
            return ("Width or Height is 0, frame can't be cropped.");
        }
        if(hasRunner){
            if(runnerInformation.getRunnerPosition().getX() == 0 || runnerInformation.getRunnerPosition().getY() == 0){
                Log.d("Benni", "SingleFrame: cropFrame(): No runner position available");
                return ("No runner position available, frame can't be cropped.");
            }
            int xStartPos = runnerInformation.getRunnerPosition().getX()-(width/2);
            int yStartPos = runnerInformation.getRunnerPosition().getY()-(height/2);

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
