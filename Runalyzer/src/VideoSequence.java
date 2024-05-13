import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class VideoSequence {
    private String videoFilePath;
    private int relativeCreationTime;
    private List<SingleFrame> singleFrames = new ArrayList<>();

    public VideoSequence(String videoFilePath, int relativeCreationTime){
        this.videoFilePath = videoFilePath;
        this.relativeCreationTime = relativeCreationTime;
    }

    public List<SingleFrame> getSingleFrames(){
        return singleFrames;
    }

    public void separateToFrames(){
        VideoFrameProcessor videoFrames = new VideoFrameProcessor();
        videoFrames.videoToFrames(videoFilePath, relativeCreationTime);
        singleFrames = videoFrames.getFrameList();
    }

    public void detectRunnerInformation(){
        Mat backgroundFrame = singleFrames.getFirst().getFrame();
        for(SingleFrame frame : singleFrames){
            frame.detectRunnerInformation(backgroundFrame);
        }
    }

    public int getMaxRunnerWidth(){
        int maxRunnerWidth = 0;
        for(SingleFrame frame : singleFrames){
            if(frame.getRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = frame.getRunnerWidth();
            }
        }
        return maxRunnerWidth;
    }

    public int getMaxRunnerHeight(){
        int maxRunnerHeight = 0;
        for(SingleFrame frame : singleFrames){
            if(frame.getRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = frame.getRunnerWidth();
            }
        }
        return maxRunnerHeight;
    }

    public void cropFrames(int width, int height){
        for(SingleFrame frame : singleFrames){
            frame.cropFrame(width, height);
        }
    }
}
