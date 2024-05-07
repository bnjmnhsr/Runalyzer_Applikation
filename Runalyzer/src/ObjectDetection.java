import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectDetection {
    private static int rect_i = 0;
    private int width;
    private int height;

    private MatOfPoint finalContour = null;
    private Rect rect;
    public void detectObject(Mat dst) {
        Rect rectCrop = null;
        Mat cannyOutput = new Mat();
        Imgproc.Canny(dst, cannyOutput, 100, 100 * 2);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        //System.out.println("Amount of Contours detected: " + contours.size());
        MatOfPoint temp_contour; //the largest is at the index 0 for starting point

        // Code to detect all contours
        Mat contourImage = dst.clone();
        double maxArea = -1;

        for (int i = 0; i < contours.size(); i++) {
            Rect boundingRect = Imgproc.boundingRect(contours.get(i));
            // Calculate the area of the contour
            //double area = Imgproc.contourArea(contours.get(i));
            double boundingRectArea = boundingRect.width * boundingRect.height;

            //System.out.println("I: "+ i + " Area: "+area);
            //System.out.println("I: " + i + " Bounding Rectangle Area: " + boundingRectArea);

            //Imgproc.drawContours(contourImage, Collections.singletonList(contours.get(i)), -1, new Scalar(0, 255, 0), 2);

            //Imgcodecs.imwrite("./contour_folder/" + i + "_contour_image_largest.jpg", contourImage);

            // Check if the current contour has a larger area than the previous largest contour
            if (boundingRectArea > maxArea) {
                maxArea = boundingRectArea;
                // Find the bounding rectangle of the contour
                rect = Imgproc.boundingRect(contours.get(i));


                //finalContour = contours.get(i);
                //System.out.println("Area: "+Imgproc.contourArea(finalContour));
            }
        }

        if (rect != null) {
            width = rect.width;
            height = rect.height;
            //System.out.println("Successful");

            // Draw the rectangle on a copy of the original image
            Mat output = new Mat();
            dst.copyTo(output);
            Imgproc.rectangle(output, rect.tl(), rect.br(), new Scalar(227, 61, 148), 2);

            // Save the image with the rectangle
            String filename = "object_" + rect_i + ".jpg";
            Imgcodecs.imwrite("./rect_folder/" + filename, output);
            rect_i++;

        } else {
            //System.out.println("Fail");
            width = 0;
            height = 0;
        }

            /*
            for (int idx = 0; idx < contours.size(); idx++) {
                temp_contour = contours.get(idx);
                MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                int contourSize = (int) temp_contour.total();
                MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.05, true);

                if (approxCurve_temp.total() == 8) {
                    MatOfPoint points = new MatOfPoint(approxCurve_temp.toArray());
                    rect = Imgproc.boundingRect(points);
                    //Imgproc.rectangle(img2, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(227, 61, 148), 2);
                    //rectCrop = new Rect(rect.x-30, 0, rect.width+60, img2.height());
                }
            }
            */


//
//        for (int i = 0; i < contours.size(); i++) {
//            Imgproc.drawContours(contourImage, contours, i, new Scalar(0, 255, 0), 2);
//        }
//
//        Imgcodecs.imwrite("./contour_folder/contour_image.jpg", contourImage);







        //Imgproc.drawContours(contourImage, Collections.singletonList(finalContour), -1, new Scalar(0, 255, 0), 2);
        //Imgcodecs.imwrite("./contour_folder/contour_image_largest.jpg", contourImage);




            /*
            // Initialize variables to store the index of the largest contour and its bounding rectangle
            int largestContourIndex = -1;
            double largestContourArea = 0;

            // Iterate through all contours
            for (int idx = 0; idx < contours.size(); idx++) {
                // Get the area of the current contour
                double contourArea = Imgproc.contourArea(contours.get(idx));

                // Check if the contour is larger than the previous largest contour
                if (contourArea > largestContourArea) {
                    largestContourArea = contourArea;
                    largestContourIndex = idx;
                }
            }

            // Check if a valid largest contour was found
            if (largestContourIndex != -1) {
                // Get the bounding rectangle of the largest contour
                MatOfPoint largestContour = contours.get(largestContourIndex);
                rect = Imgproc.boundingRect(largestContour);

                // Draw the rectangle on a copy of the original image
                Mat output = new Mat();
                dst.copyTo(output);
                Imgproc.rectangle(output, rect.tl(), rect.br(), new Scalar(227, 61, 148), 2);

                // Save the image with the rectangle
                String filename = "object_" + rect_i + ".jpg";
                Imgcodecs.imwrite("./rect_folder/" + filename, output);
                rect_i++;

                // Update width and height after the loop
                width = rect.width;
                height = rect.height;
            }
             */
    }

    public int getObjectWidth(){
        return 400;
    }

    public int getObjectHeight(){
        return 700;
    }
}
