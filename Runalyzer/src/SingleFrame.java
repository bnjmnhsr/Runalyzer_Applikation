import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class SingleFrame {
    private Mat frame;
    private Mat croppedFrame = null;
    private boolean hasRunner;
    private Vector runnerPosition;
    private int runnerWidth;
    private int runnerHeight;
    private int timecode;

    public SingleFrame(){
        frame = new Mat();
        hasRunner = false;
        runnerPosition = new Vector();
        runnerWidth = 0;
        runnerHeight = 0;
        timecode = 0;
    }

    public SingleFrame(Mat frame){
        this.frame = frame;
        hasRunner = false;
        runnerPosition = new Vector();
        runnerWidth = 0;
        runnerHeight = 0;
        timecode = 0;
    }

    public Mat getFrame(){
        return frame;
    }

    public Mat getCroppedFrame(){
        return croppedFrame;
    }

    public boolean hasRunner(){
        return hasRunner;
    }

    public void detectRunnerInformation(Mat backgroundFrame){
        BackgroundSubtraction backgroundSubtractor = new BackgroundSubtraction();
        Mat differenceImg = backgroundSubtractor.subtract(backgroundFrame, frame);

        Moments moments = Imgproc.moments(differenceImg);
        //get_m00 counts number of white pixels in the image, if enough pixels counted there exists a runner...
        //TODO: check what's a correct value to identify a runner (depends also on camera-distance)
        if(moments.get_m00() > 50){
            hasRunner = true;
            runnerPosition.setX((int) (moments.get_m10() / moments.get_m00()));
            runnerPosition.setY((int) (moments.get_m01() / moments.get_m00()));
            ObjectDetection objDetector = new ObjectDetection();
            objDetector.detectObject(differenceImg); // new line of code, might not work...
            runnerWidth = objDetector.getObjectWidth();
            runnerHeight = objDetector.getObjectHeight();
        }
        else{
            hasRunner = false;
        }
    }

    public int getRunnerWidth(){
        return runnerWidth;
    }

    public int getRunnerHeight(){
        return runnerHeight;
    }

    public void cropFrame(int width, int height){
        if(hasRunner){
            int xStartPos = runnerPosition.getX()-(width/2);
            int yStartPos = runnerPosition.getY()-(height/2);
            //TODO: find solution to crop Frame when Runner is at the edge
            if(xStartPos > 0 && xStartPos < (frame.width()-width)){
                if(yStartPos > 0 && yStartPos < (frame.height()-height)){
                    Rect rectCrop = new Rect(xStartPos, yStartPos, width, height);
                    croppedFrame = new Mat(frame,rectCrop);
                }
            }
        }
    }
}
