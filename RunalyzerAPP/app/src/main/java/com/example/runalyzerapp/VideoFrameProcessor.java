package com.example.runalyzerapp;

import org.opencv.android.Utils;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.videoio.Videoio.*;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;

public class VideoFrameProcessor {
    private List<SingleFrame> frameList = new ArrayList<>();

    public List<SingleFrame> getFrameList(){
        return frameList;
    }

    public void videoToFrames(String videoFilePath, int relativeCreationTime) {
        VideoCapture videoCapture = new VideoCapture(videoFilePath);

        if (!videoCapture.isOpened()) {
            Log.d("Runalyzer", "Failed to open video file: " + videoFilePath);
            return;
        }

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double millisBetweenFrames = (1000.0/videoCapture.get(CAP_PROP_FPS));
        int frameCount = 0;

        while (videoCapture.read(frame)) {
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();
            //convert frame to RGB, because colors were switched somehow...
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);

            frame.copyTo(currentFrame); //TODO: maybe remove
            frameList.add(new SingleFrame(currentFrame, timecode));
            timecode += millisBetweenFrames;
            frameCount++;
            Log.d("Runalyzer", "Frame count: " + frameCount + ", Timecode: " + timecode);
            //store frame image in storage
            //Imgcodecs.imwrite("/storage/emulated/0/Pictures/" + "frame" + frameCount + ".jpg", currentFrame);

            //I added this because the program crashed
            //if (frameCount == 30) {
            //    break;
            //}
        }

        videoCapture.release();
    }

    public void extractAllFrames(Context context, Uri videoUri, int relativeCreationTime) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try {
            retriever.setDataSource(context, videoUri);

            //Get Video Properties

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long durationMillis = Long.parseLong(durationStr);

            String frameRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            double frameRate;
            if (frameRateStr != null && !frameRateStr.isEmpty()) {
                frameRate = Double.parseDouble(frameRateStr);
                Log.d("Benni", "Detected Frame Rate: " + frameRate + " fps");
            } else {
                frameRate = 60.0;
                Log.d("Benni", "Frame Rate not found, using default: " + frameRate + " fps");
            }

            double millisBetweenFrames = 1000.0 / frameRate;

            //Extract Frames

            Mat frame = new Mat();
            double timecode = relativeCreationTime;

            for (double t = 0; t < durationMillis; t += millisBetweenFrames) {
                Bitmap bitmapFrame = retriever.getFrameAtTime((long)t,
                        MediaMetadataRetriever.OPTION_CLOSEST);
                if (bitmapFrame != null) {
                    Utils.bitmapToMat(bitmapFrame, frame);
                    //Add relativeCreationTime to the calculated timecode
                    frameList.add(new SingleFrame(frame.clone(), timecode + relativeCreationTime));
                    Log.d("Benni", "Frame count: " + frameList.size() + ", Timecode: " + timecode);
                }
                timecode += millisBetweenFrames;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void framesToVideo(List<Mat> frames, int videoWidth, int videoHeight) {
        int fourcc = VideoWriter.fourcc('H', '2', '6', '4');
        Size size = new Size(videoWidth, videoHeight);
        VideoWriter videoWriter = new VideoWriter("/sdcard/DCIM/Camera/video.mp4", fourcc, 30, size);

        if (videoWriter.isOpened()) {
            Log.d("Runalyzer", "VideoWriter opened successfully");
        } else {
            Log.d("Runalyzer", "Failed to open VideoWriter");
        }

        for (Mat frame : frames) {
            if(frame != null){
                videoWriter.write(frame);
                Log.d("Runalyzer", "Frame written");
            }
        }
        Log.d("Runalyzer", "Video written");
        videoWriter.release();
    }

    //that was an idea, if we can still work with real paths we can use this to find it...
    public String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
    }
}
