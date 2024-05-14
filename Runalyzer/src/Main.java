import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Runalyzer runalyzer = new Runalyzer();
        runalyzer.loadVideoFiles("./input_videos/");
        runalyzer.detectRunnerInformation();
        runalyzer.detectMaxRunnerWidthHeight();
        runalyzer.cropSingleFrames();
        runalyzer.createFinalVideo();
    }
}
