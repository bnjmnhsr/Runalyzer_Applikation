import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoWriter;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.videoio.Videoio.*;

public class VideoFrameProcessor {
    private List<SingleFrame> frameList = new ArrayList<>();

    public List<SingleFrame> getFrameList(){
        return frameList;
    }

    public void videoToFrames(String videoFilePath, int relativeCreationTime) {
        VideoCapture videoCapture = new VideoCapture(videoFilePath);

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double millisBetweenFrames = (1000.0/videoCapture.get(CAP_PROP_FPS));

        while (videoCapture.read(frame)) {
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();
            frame.copyTo(currentFrame); //TODO: maybe remove
            frameList.add(new SingleFrame(currentFrame, timecode));
            timecode += millisBetweenFrames;
        }
        System.out.println("Size of the Video in Frames: " + frameList.size()); //TODO: only for debugging, remove

        videoCapture.release();
    }

    public void framesToVideo(List<Mat> frames, int videoWidth, int videoHeight) {
        int fourcc = VideoWriter.fourcc('M', 'P', '4', 'V');
        Size size = new Size(videoWidth, videoHeight);
        VideoWriter videoWriter = new VideoWriter("output_video.mp4", fourcc, 30, size);

        for (Mat frame : frames) {
            videoWriter.write(frame);
        }
        videoWriter.release();
    }
}
