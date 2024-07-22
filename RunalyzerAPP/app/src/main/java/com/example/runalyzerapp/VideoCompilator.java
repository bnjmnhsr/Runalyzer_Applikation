package com.example.runalyzerapp;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoCompilator {
    private List<Mat> finalFrames = new ArrayList<>();
    private int videoWidth;
    private int videoHeight;

    public VideoCompilator(int videoWidth, int videoHeight){
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public String selectFinalFrames(List<VideoSequence> videoSequences){
        Log.d("Benni", "Selecting Final Frames");
        if(videoSequences.isEmpty()){
            Log.d("Benni", "VideoCompilator: selectFinalFrames(): No video sequences");
            return ("No video sequences to select final frames.");
        }
        double relativeRunnerPosition;
        double switchingTimecode = 0;
        for(VideoSequence vs : videoSequences){
            if(vs.getSingleFrames().isEmpty()){
                Log.d("Benni", "VideoCompilator: selectFinalFrames(): No single frames in video sequence");
                return ("No single frames in video sequence, final frames can't be selected.");
            }
            for(SingleFrame fr : vs.getSingleFrames()){
                if(fr.hasRunner() && fr.getTimecode() >= switchingTimecode){
                    if(fr.getRunnerPosition().getX() == 0 || fr.getFrame().width() == 0){
                        Log.d("Benni", "VideoCompilator: selectFinalFrames(): Runner position or frame width is 0");
                        return ("Runner position or frame width is 0, final frame can't be selected.");
                    }
                    relativeRunnerPosition = (double) fr.getRunnerPosition().getX() / fr.getFrame().width();
                    if(vs != videoSequences.get(videoSequences.size() - 1) && relativeRunnerPosition >= 0.875){
                        switchingTimecode = fr.getTimecode();
                        break;
                    }
                    else{
                        if(fr.getCroppedFrame() != null){
                            finalFrames.add(fr.getCroppedFrame());
                        }
                    }
                }
                //TODO: added if we just want to create a video without cropping or selection
                //finalFrames.add(fr.getFrame());
                //Log.d("Benni", "Frame added to finalFrames");
            }
        }
        return "success";
    }

    public String createFinalVideo(){
        String retval = null;
        VideoFrameProcessor processorFinalVideo = new VideoFrameProcessor();
        if(!finalFrames.isEmpty()){
            retval = processorFinalVideo.framesToVideo(finalFrames);
            if(!Objects.equals(retval, "success")){
                return retval;
            }
        }else{
            Log.d("Benni", "VideoCompilator: createFinalVideo(): No final frames");
            return ("No final frames available, final video can't be created.");
        }
        return "success";
    }
}
