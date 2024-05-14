import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Runalyzer {
    private File[] inputVideoFiles;
    private List<VideoSequence> videoSequences;
    private int[] millisCreationTime;
    private int maxRunnerWidth;
    private int maxRunnerHeight;
    private int croppingWidth;
    private int croppingHeight;

    public Runalyzer(){
        this.maxRunnerWidth = 0;
        this.maxRunnerHeight = 0;
    }

    public void loadVideoFiles(String filePath){
        inputVideoFiles = new File(filePath).listFiles();
        //TODO: get millisCreationTime in another way
        millisCreationTime[0] = 0;      //vid01
        millisCreationTime[1] = 4087;   //vid02
    }

    public void detectRunnerInformation(){
        videoSequences = new ArrayList<>();
        for(int i = 0; i < inputVideoFiles.length; i++){
            videoSequences.add(new VideoSequence(inputVideoFiles[i].getPath(), millisCreationTime[i]));
            videoSequences.get(i).separateToFrames();
            videoSequences.get(i).detectRunnerInformation();
        }
    }

    public void detectMaxRunnerWidthHeight(){
        for(VideoSequence vidSequence : videoSequences){
            if(vidSequence.getMaxRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = vidSequence.getMaxRunnerWidth();
            }
            if(vidSequence.getMaxRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = vidSequence.getMaxRunnerHeight();
            }
        }
    }

    public void cropSingleFrames(){
        croppingWidth = maxRunnerWidth + 10;
        croppingHeight = maxRunnerHeight + 10;
        for(VideoSequence vidSequence : videoSequences){
            vidSequence.cropFrames(croppingWidth, croppingHeight);
        }
    }

    public void createFinalVideo(){
        VideoCompilator videoCompilator = new VideoCompilator(croppingWidth, croppingHeight);
        videoCompilator.selectFinalFrames(videoSequences);
        videoCompilator.createFinalVideo();
    }
}
