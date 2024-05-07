import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Runalyzer {
    public Runalyzer(){
        //Load files into a File list called inputVideoFiles
        File[] inputVideoFiles = new File("./input_videos/").listFiles();
        List<VideoSequence> videoSequences = new ArrayList<>();

        //Extract information for each video sequence
        for(int i = 0; i < inputVideoFiles.length; i++){
            videoSequences.add(new VideoSequence(inputVideoFiles[i].getPath()));
            videoSequences.get(i).separateToFrames();
            videoSequences.get(i).detectRunnerInformation();
        }

        //Detect max Runner-Width/-Height
        int maxRunnerWidth = 0;
        int maxRunnerHeight = 0;
        for(VideoSequence vidSequence : videoSequences){
            if(vidSequence.getMaxRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = vidSequence.getMaxRunnerWidth();
            }
            if(vidSequence.getMaxRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = vidSequence.getMaxRunnerHeight();
            }
        }

        //Crop Single-Frames
        int croppingWidth = maxRunnerWidth + 10;
        int croppingHeight = maxRunnerHeight + 10;
        for(VideoSequence vidSequence : videoSequences){
            vidSequence.cropFrames(croppingWidth, croppingHeight);
        }

        //Synthesize cropped Single-Frames to final Video-Compilation
        VideoCompilator videoCompilator = new VideoCompilator(croppingWidth, croppingHeight);
        videoCompilator.selectFinalFrames(videoSequences);
        videoCompilator.createFinalVideo();
    }
}
