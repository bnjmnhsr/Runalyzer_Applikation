import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoWriter;

import java.util.ArrayList;
import java.util.List;

public class VideoFrameProcessor {
    private List<Mat> frameList = new ArrayList<>();

    public List<Mat> getFrameList(){
        return frameList;
    }

    public void videoToFrames(String videoFilePath) {
        VideoCapture videoCapture = new VideoCapture(videoFilePath);

        Mat frame = new Mat();

        while (videoCapture.read(frame)) {
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();
            frame.copyTo(currentFrame);

            frameList.add(currentFrame);
        }
        System.out.println("Size of the Video in Frames: " + frameList.size()); //only for debugging

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
