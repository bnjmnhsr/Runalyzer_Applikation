package com.example.runalyzerapp;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.opencv.android.Utils;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public String videoToFrames(Context context, String videoFilePath, int relativeCreationTime) {
        if(Objects.equals(videoFilePath, "")){
            Log.d("Benni","VideoFrameProcessor: videoToFrames(): Empty videoFilePath");
            return("Empty video file path. Probably no Uri detected.");
        }
        VideoCapture videoCapture = null;
        try {
            videoCapture = new VideoCapture(videoFilePath);
        } catch (Exception e) {
            Log.e("Benni", Log.getStackTraceString(e));
            return Log.getStackTraceString(e);
        }

        if (!videoCapture.isOpened()) {
            Log.d("Benni", "VideoFrameProcessor: videoToFrames(): Failed to open video file: " + videoFilePath);
            return ("Failed to open video file: " + videoFilePath);
        }

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double fps = videoCapture.get(CAP_PROP_FPS);
        if(fps == 0){
            Log.d("Benni","VideoFrameProcessor: videoToFrames(): fps can't be read from input video");
            return("fps can't be read from input video");
        }
        double millisBetweenFrames = (1000.0/fps);

        while (videoCapture.read(frame)) {
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();

            frame.copyTo(currentFrame); //TODO: maybe remove
            double imgRatio = (double)currentFrame.height()/(double)currentFrame.width();
            Imgproc.resize(currentFrame, currentFrame, new Size(500,500*imgRatio));
            frameList.add(new SingleFrame(currentFrame, timecode));
            timecode += millisBetweenFrames;
        }
        if(frameList.isEmpty()){
            Log.d("Benni","VideoFrameProcessor: videoToFrames(): frameList still empty, videoCapture failed");
            return("No single frame could be captured from input Video.");
        }

        frame.release();
        videoCapture.release();
        return ("success");
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

    public String framesToVideo(List<Mat> frames) {
        Log.d("Benni", "Starting Frames To Video Encoding");
        if(frames.isEmpty()){
            Log.d("Benni","VideoFrameProcessor: framesToVideo(): frames empty, video can't be created");
            return("No frames to create final video.");
        }
        FileChannelWrapper out = null;
        try {
            File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String moviesdir = moviesDir.getAbsolutePath();
            //output file with data and time stamp
            out = NIOUtils.writableFileChannel(moviesdir + "/VideoCompilation_" + System.currentTimeMillis() + ".mp4");
            AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(15, 1));
            for (Mat frame : frames) {
                // Convert the Mat to a Bitmap
                Bitmap image = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(frame, image);
                // Encode the image
                encoder.encodeImage(image);
            }
            // Finalize the encoding, i.e. clear the buffers, write the header, etc.
            encoder.finish();
            Log.d("Benni", "Video written");
        } catch (IOException e) {
            Log.e("Benni", "VideoFrameProcessor: framesToVideo(): " + Log.getStackTraceString(e));
            return ("Create video from frames failed. Details see log.");
        } finally {
            Log.d("Benni", "Closing File");
            NIOUtils.closeQuietly(out);
        }
        return "success";
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
