import org.opencv.core.Mat;
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
        for(VideoSequence vs : videoSequences){
            for(SingleFrame fr : vs.getSingleFrames()){
                if(fr.getCroppedFrame() != null){
                    //TODO: implement transition from video 'n' to video 'n+1'
                    finalFrames.add(fr.getCroppedFrame());
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
