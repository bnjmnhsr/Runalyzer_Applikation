package com.example.runalyzerapp;

import android.util.Log;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

//in this call, the runner Object is detected based on contour analysis with the bounding rectangle around it,
//the area around the runner should be made black to reduce the noise in the image
public class ObjectDetection {
    private Rect objectRect = null;

    //TODO for debugging, create rect_i to store the index of the bounding rectangle, it should be not change if a new object is created
    private static int rect_i = 0;

    public Mat removeNoise(Mat dst) {
        //detect runner object, based on contour analysis, the contour of the runner should be the biggest one
        //create a bounding rectangle around the runner object
        //try to remove noise in the image by making the area around the runner black

        Mat processedImg = new Mat();
        Mat kernel = new Mat();
        Mat fill_kernel = new Mat();
        Mat fill02_kernel = new Mat();
        Mat cannyOutput = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();

        try {
            //copy dst to a new matrix to avoid changing the original image
            processedImg = dst.clone();

            // Add morphological operations to fill holes, remove noise, and dilate the mask
            kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6));
            Imgproc.erode(dst, processedImg, kernel); // Apply erosion to remove small white noise
            Imgproc.morphologyEx(processedImg, processedImg, Imgproc.MORPH_OPEN, kernel); // Apply opening to remove noise

            fill_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 10));
            Imgproc.morphologyEx(processedImg, processedImg, Imgproc.MORPH_CLOSE, fill_kernel); // Apply closing to fill small holes in the foreground

            fill02_kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(23, 23));
            Imgproc.dilate(processedImg, processedImg, fill02_kernel); // Apply dilation to make the object more visible

            Imgproc.Canny(processedImg, cannyOutput, 100, 100 * 2);

            //List<MatOfPoint> contours = new ArrayList<>(); //create a list to store the contours
            Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE); //find the contours in the image

            double maxArea = -1; //initialize the maximum area to -1

            // detect the biggest contour based on the area of the bounding rectangle around it
            for (MatOfPoint contour : contours) {
                Rect boundingRect = Imgproc.boundingRect(contour); //create a bounding rectangle around the contour
                double boundingRectArea = boundingRect.width * boundingRect.height; //calculate the area of the bounding rectangle

                //check if the current bounding rectangle has a larger area than the previous largest bounding rectangle
                if (boundingRectArea > maxArea) {
                    objectRect = boundingRect; //store the bounding rectangle
                    maxArea = boundingRectArea; //update the maximum area
                }
            }

            // TODO for debugging purposes, print the area of the bounding rectangle and store it
//        if (objectRect != null) {
//            //draw the bounding rectangle on a copy of the original image
//            Mat output = new Mat();
//            processedImg.copyTo(output);
//            Imgproc.rectangle(output, objectRect.tl(), objectRect.br(), new Scalar(227, 61, 148), 2);
//
//            //save the image with the bounding rectangle
//            Imgcodecs.imwrite("./rect_folder/object_" + rect_i + ".jpg", output);
//            rect_i++;
//        }

            //remove noise in the image by making the area around the runner black
            if (objectRect != null) {
                // Get the center of the current bounding rectangle
                Point center = new Point(objectRect.x + objectRect.width / 2.0, objectRect.y + objectRect.height / 2.0);

                // Define the new width and height
                int width = (int) (objectRect.width * 3.2); // Change this to your desired width
                int height = (int) (objectRect.height * 2.8); // Change this to your desired height

                // Calculate the new top-left corner position
                int newX = (int) (center.x - width / 2.0);
                int newY = (int) (center.y - height / 2.5);

                // Create the new bounding rectangle
                Rect newObjectRect = new Rect(newX, newY, width, height);


                //create a mask to make the area around the runner black
                Mat mask = new Mat(dst.size(), CvType.CV_8UC1, new Scalar(0));
                Imgproc.rectangle(mask, newObjectRect.tl(), newObjectRect.br(), new Scalar(255), -1); //fill the bounding rectangle with white

                //apply the mask to the image
                Core.bitwise_and(dst, mask, dst);

                mask.release();

                //TODO for debugging purposes, print the area of the bounding rectangle#
                //Draw the new bounding rectangle on a copy of the original image for debugging
//            Mat output_2 = new Mat();
//            dst.copyTo(output_2);
//            Imgproc.rectangle(output_2, newObjectRect.tl(), newObjectRect.br(), new Scalar(227, 61, 148), 2);

                // Save the image with the new bounding rectangle
                //Imgcodecs.imwrite("./rect_folder/new_object_" + rect_i + ".jpg", output_2);
//            Moments moments = Imgproc.moments(dst);
//            System.out.println("m00: " + moments.get_m00());
            }
        }catch(Exception e){
            Log.e("Benni", Log.getStackTraceString(e));
        }finally {
            processedImg.release();
            kernel.release();
            fill_kernel.release();
            fill02_kernel.release();
            cannyOutput.release();
            hierarchy.release();
            for (MatOfPoint contour : contours) {
                contour.release();
            }
            System.gc();
        }

        return dst;
    }

    public int getObjectWidth(){
        if (objectRect != null) {
            return objectRect.width * 3;
        }
        return 0;
    }

    public int getObjectHeight(){
        if (objectRect != null) {
            return objectRect.height * 3;
        }
        return 0;
    }
}
