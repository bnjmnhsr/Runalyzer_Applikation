package com.example.runalyzerapp;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import java.util.List;

public class Activity_CreateCompilation extends AppCompatActivity {
    private int[] millisCreationTime;
    private List<Uri> inputVideoUris;
    private WorkerUsingThread runalyzerThread;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_compilation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Registers a photo picker activity launcher in multi-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());

                        //TODO: get millisCreationTime in another way
                        millisCreationTime = new int[uris.size()];
                        millisCreationTime[0] = 0;      //vid01
                        millisCreationTime[1] = 4087;   //vid02

                        inputVideoUris = uris;

                        Log.d("Benni", "Starting runalyzerThread");
                        statusText = (TextView) findViewById(R.id.textView_status);
                        statusText.setText("Starting Runalyzer...");
                        runalyzerThread = new WorkerUsingThread();
                        runalyzerThread.start();

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());
    }

    class WorkerUsingThread implements Runnable {
        private volatile boolean running = false;
        private Thread thread;

        private void print(final String s){
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    statusText.setText(s);
                }
            });
        }

        @Override
        public void run() {
            Runalyzer runalyzer = new Runalyzer(inputVideoUris, millisCreationTime);
            if(running){
                print("Detecting runner-information...");
                runalyzer.detectRunnerInformation(Activity_CreateCompilation.this);
                print("Detecting maximum runner-width and -height...");
                runalyzer.detectMaxRunnerWidthHeight();
                print("Cropping all single frames...");
                runalyzer.cropSingleFrames();
                print("Creating final video...");
                runalyzer.createFinalVideo();
                print("Finished!");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                runalyzerThread.stop();
            }
        }

        void start() {
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        void stop(){
            if(!running){
                //TODO
            }else{
                running = false;
                while(true){
                    try {
                        thread.join();
                        break;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }


}