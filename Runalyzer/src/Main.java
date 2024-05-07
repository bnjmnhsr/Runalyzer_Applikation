import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Runalyzer runalyzer = new Runalyzer();

//        VideoFrameProcessor vfp = new VideoFrameProcessor();
//        vfp.videoToFrames("./input_videos/Video_001.mov");
//
//        BackgroundSubtraction bs = new BackgroundSubtraction();
//        Mat result = bs.subtract(vfp.getFrameList().getFirst(), vfp.getFrameList().get(156));
//        Imgcodecs.imwrite("./out_result.jpg", result);
//
//        ObjectDetection od = new ObjectDetection();
//        od.detectObject(Imgcodecs.imread("./out_result.jpg"));
    }
}
