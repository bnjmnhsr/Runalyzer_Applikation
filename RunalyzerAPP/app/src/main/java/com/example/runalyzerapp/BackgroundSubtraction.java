package com.example.runalyzerapp;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class BackgroundSubtraction {

    //Use to check Substracted images
    private static int resultimg_i = 0; //for debugging only!!!
    public Mat subtract(Mat background, Mat img) {
        Mat backgroundGray = new Mat();
        Mat imgGray = new Mat();
        Imgproc.cvtColor(background, backgroundGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(img, imgGray, Imgproc.COLOR_RGB2GRAY);

        Mat backgroundGrayBlurred = new Mat();
        Mat imgGrayBlurred = new Mat();
        Imgproc.medianBlur(backgroundGray, backgroundGrayBlurred, 3);
        Imgproc.medianBlur(imgGray, imgGrayBlurred, 3);

        if (false) { //TODO: add requirement for inverting (maybe black shirt)
            Core.bitwise_not(backgroundGrayBlurred, backgroundGrayBlurred);
            Core.bitwise_not(imgGrayBlurred, imgGrayBlurred);
        }

        Mat resultImg = new Mat();
        Core.subtract(imgGrayBlurred, backgroundGrayBlurred, resultImg);
        Imgproc.medianBlur(resultImg, resultImg, 3); //davor 7

        int thresholdValue = 35; // 0-255, was 35 before
        int thresholdType = 0; // 0: Binary, 1: Binary Inverted, 2: Truncate, 3: To Zero, 4: To Zero Inverted
        Imgproc.threshold(resultImg, resultImg, thresholdValue, 255, thresholdType);


        // Iterate over each row in the image
//        for (int y = 0; y < resultImg.rows(); y++) {
//            // Check if the current row is above the threshold Y value
//            if (y < 400) {
//                // Set all pixel values in the current row to zero
//                for (int x = 0; x < resultImg.cols(); x++) {
//                    resultImg.put(y, x, 0); // Set pixel value to zero
//                }
//            }
//        }

        // Add morphological operations to fill holes, remove noise, and dilate the mask
        //Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        //Imgproc.morphologyEx(resultImg, resultImg, Imgproc.MORPH_CLOSE, kernel); // fill holes
        //Imgproc.dilate(resultImg, resultImg, kernel);


        //Use to check Substracted images
        Imgcodecs.imwrite("./background_test/out"+resultimg_i+".jpg", resultImg);
        resultimg_i++;


        return resultImg;
    }
}