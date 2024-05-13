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
        double relativeRunnerPosition;
        double switchingTimecode = 0;
        for(VideoSequence vs : videoSequences){
            for(SingleFrame fr : vs.getSingleFrames()){
                if(fr.hasRunner() && fr.getTimecode() >= switchingTimecode){
                    relativeRunnerPosition = (double) fr.getRunnerPosition().getX() / fr.getFrame().width();
                    if(vs != videoSequences.getLast() && relativeRunnerPosition >= 0.875){
                        switchingTimecode = fr.getTimecode();
                        break;
                    }
                    else{
                        finalFrames.add(fr.getCroppedFrame());
                    }
                }
            }
        }
    }

    public void createFinalVideo(){
        VideoFrameProcessor processorFinalVideo = new VideoFrameProcessor();
        if(!finalFrames.isEmpty()){
            processorFinalVideo.framesToVideo(finalFrames, videoWidth, videoHeight);
        }else{
            System.out.println("VideoCompilator: createFinalVideo: No finalFrames existing."); //TODO: change before release
        }
    }
}
