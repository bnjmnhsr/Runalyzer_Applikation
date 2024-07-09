package com.example.runalyzerapp;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

public class VideoCompilator {
    private List<Mat> finalFrames = new ArrayList<>();
    private int videoWidth;
    private int videoHeight;

    public VideoCompilator(int videoWidth, int videoHeight){
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
    }

    public void selectFinalFrames(List<VideoSequence> videoSequences){
        Log.d("Benni", "Selecting Final Frames");
        double relativeRunnerPosition;
        double switchingTimecode = 0;
        for(VideoSequence vs : videoSequences){
            for(SingleFrame fr : vs.getSingleFrames()){
                if(fr.hasRunner() && fr.getTimecode() >= switchingTimecode){
                    relativeRunnerPosition = (double) fr.getRunnerPosition().getX() / fr.getFrame().width();
                    if(vs != videoSequences.get(videoSequences.size() - 1) && relativeRunnerPosition >= 0.875){
                        switchingTimecode = fr.getTimecode();
                        break;
                    }
                    else{
                        if(fr.getCroppedFrame() != null){
                            finalFrames.add(fr.getCroppedFrame());
                            Log.d("Benni", "Cropped Frame added to finalFrames");
                        }
                    }
                }
                //TODO: added if we just want to create a video without cropping or selection
                //finalFrames.add(fr.getFrame());
                //Log.d("Benni", "Frame added to finalFrames");
            }
        }
    }

    public void createFinalVideo(){
        VideoFrameProcessor processorFinalVideo = new VideoFrameProcessor();
        if(!finalFrames.isEmpty()){
            processorFinalVideo.framesToVideo(finalFrames);
        }else{
            //TODO: error handling
        }
    }
}
