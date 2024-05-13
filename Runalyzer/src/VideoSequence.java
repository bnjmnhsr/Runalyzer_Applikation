import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

public class VideoSequence {
    private String videoFilePath;
    private List<SingleFrame> singleFrames = new ArrayList<>();

    //Construction that initiates the object VideoSequence with the videofilepath
    public VideoSequence(String videoFilePath){
        this.videoFilePath = videoFilePath;
    }

    public List<SingleFrame> getSingleFrames(){
        return singleFrames;
    }

    public void separateToFrames(){
        VideoFrameProcessor videoFrames = new VideoFrameProcessor();
        videoFrames.videoToFrames(this.videoFilePath);
        List<Mat> frameList = videoFrames.getFrameList();

        for(Mat frame : frameList){
            singleFrames.add(new SingleFrame(frame));
        }
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
                maxRunnerHeight = frame.getRunnerHeight();
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
