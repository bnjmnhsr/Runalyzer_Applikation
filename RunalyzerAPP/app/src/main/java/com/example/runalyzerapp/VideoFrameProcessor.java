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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.videoio.Videoio.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;

public class VideoFrameProcessor {
    private List<SingleFrame> frameList = new ArrayList<>();

    public List<SingleFrame> getFrameList(){
        return frameList;
    }

    public void videoToFrames(Context context, String videoFilePath, int relativeCreationTime) {
        VideoCapture videoCapture = null;
        try {
            videoCapture = new VideoCapture(videoFilePath);
        } catch (Exception e) {
            Log.e("Benni", Log.getStackTraceString(e));
            return;
        }

        if (!videoCapture.isOpened()) {
            Log.d("Benni", "Failed to open video file: " + videoFilePath);
            return;
        }

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double millisBetweenFrames = (1000.0/videoCapture.get(CAP_PROP_FPS));

        while (videoCapture.read(frame)) {
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();

            frame.copyTo(currentFrame); //TODO: maybe remove
            double imgRatio = (double)currentFrame.height()/(double)currentFrame.width();
            Imgproc.resize(currentFrame, currentFrame, new Size(500,500*imgRatio));
            frameList.add(new SingleFrame(currentFrame, timecode));
            timecode += millisBetweenFrames;
        }

        frame.release();
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
                Bitmap bitmapFrame = retriever.getFrameAtTime((long)t*1000, MediaMetadataRetriever.OPTION_CLOSEST);
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
        int fourcc = VideoWriter.fourcc('M', 'J', 'P', 'G');
        Size size = new Size(videoWidth, videoHeight);
        VideoWriter videoWriter = new VideoWriter(Environment.getExternalStorageDirectory().getPath() + "/output.avi", fourcc, 30, size);

        if (videoWriter.isOpened()) {
            Log.d("Benni", "VideoWriter opened successfully");
        } else {
            Log.d("Benni", "Failed to open VideoWriter");
        }

        for (Mat frame : frames) {
            if(frame != null){
                videoWriter.write(frame);
                Log.d("Benni", "Frame written");
            }
        }
        Log.d("Benni", "Video written");
        videoWriter.release();
    }


    //TODO: only for debugging, remove
    public void saveBitmapToGallery(Context context, String filename, Bitmap bitmap) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri imageUri = context.getContentResolver().insert(externalContentUri, values);

        try {
            if (imageUri != null) {
                OutputStream outputStream = context.getContentResolver().openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 20, outputStream);
                    outputStream.close();
                    Log.d("Benni", filename + " saved to gallery");
                } else {
                    Log.e("Benni", "OutputStream is null.");
                }
            } else {
                Log.e("Benni", "ImageUri is null.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Benni", Log.getStackTraceString(e));
        }
    }
}
