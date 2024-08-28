package com.example.runalyzerapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Activity_CreateCompilation extends AppCompatActivity {
    private int[] millisCreationTime;
    private List<Uri> inputVideoUris;
    private List<Uri> videoPickerUris;
    private WorkerUsingThread runalyzerThread;
    private TextView statusText;
    private TextView infoText;
    private VideoView videoView1;
    private EditText editTextVideo1;
    private int[] video_pos;
    private int selector = 0;

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

        videoView1 = findViewById(R.id.videoView1);
        editTextVideo1 = findViewById(R.id.editTextVideo1);

        Button button1 = findViewById(R.id.buttonEditText1);
        Button button3 = findViewById(R.id.buttonTextView);

        button1.setEnabled(false);
        button3.setEnabled(false);

        Spinner spinner1 = findViewById(R.id.number_spinner1);
        statusText = (TextView) findViewById(R.id.textView_status);
        statusText.setText(R.string.info_screen);

        infoText = (TextView) findViewById(R.id.textInfo);
        if(selector == 0) {
            infoText.setText(R.string.first_video);
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.numbers_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner1.setAdapter(adapter);
        //spinner2.setAdapter(adapter);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selector >= inputVideoUris.size()) {
                    Toast.makeText(Activity_CreateCompilation.this, "Finished Gathering the Data", Toast.LENGTH_LONG).show();
                    button3.setEnabled(true);
                    return;
                }

                String timeInput = editTextVideo1.getText().toString();

                if(!timeInput.matches("\\d{2}:\\d{2}:\\d{2}\\.\\d{3}")) {
                    Toast.makeText(Activity_CreateCompilation.this, "Invalid input. Please enter a valid time in the format HH:MM:SS.mmm", Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    String[] timeParts = timeInput.split("[:.]");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);
                    int milliseconds = Integer.parseInt(timeParts[3]);

                    int totalMilliseconds = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000) + milliseconds;

                    if (millisCreationTime != null && inputVideoUris != null) {
                        int selectedPos = Integer.parseInt((String) spinner1.getSelectedItem()) - 1;

                        if (selectedPos < inputVideoUris.size()) {
                            if (Arrays.stream(video_pos, 0, selector).anyMatch(pos -> pos == selectedPos)) {
                                Toast.makeText(Activity_CreateCompilation.this, "Position already used", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } else if ( (selectedPos + 1) > inputVideoUris.size()) {
                            Toast.makeText(Activity_CreateCompilation.this, "Invalid position", Toast.LENGTH_LONG).show();
                            return;
                        }

                        video_pos[selector] = selectedPos;
                        inputVideoUris.set(selectedPos, videoPickerUris.get(selector));
                        millisCreationTime[selectedPos] = totalMilliseconds;

                        selector++;
                        if (selector < videoPickerUris.size()) {
                            switch (selector) {
                                case 1:
                                    infoText.setText(R.string.second_video);
                                    break;
                                case 2:
                                    infoText.setText(R.string.third_video);
                                    break;
                                case 3:
                                    infoText.setText(R.string.fourth_video);
                                    break;
                                default:
                                    break;
                            }
                            videoView1.setVideoURI(videoPickerUris.get(selector));
                            videoView1.start();
                        } else {
                            Toast.makeText(Activity_CreateCompilation.this, "Finished Gathering the Data", Toast.LENGTH_LONG).show();
                            button3.setEnabled(true);
                            infoText.setText(R.string.video_done);
                            button1.setEnabled(false);
                        }
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(Activity_CreateCompilation.this, "Invalid time input.", Toast.LENGTH_LONG).show();
                }
            }

        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Benni", "Starting runalyzerThread");
                statusText.setText("Starting Runalyzer...");
                runalyzerThread = new WorkerUsingThread(Activity_CreateCompilation.this);
                runalyzerThread.start();
            }
        });


        // Registers a photo picker activity launcher in multi-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());
                        videoPickerUris = uris;
                        inputVideoUris = new ArrayList<>(Collections.nCopies(videoPickerUris.size(), null));

                        //TODO: get millisCreationTime in another way
                        millisCreationTime = new int[uris.size()];
                        video_pos = new int[uris.size()];

                        Arrays.fill(video_pos, -1);
                        Arrays.fill(millisCreationTime, -1);

                        button1.setEnabled(true);

                        videoView1.setVideoURI(uris.get(0));
                        videoView1.start();
                    } else {
                        Log.d("PhotoPicker", "No media selected");
                        finish();
                    }
                });

        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());
    }
    //on resumne
    @Override
    protected void onResume() {
        super.onResume();
        if(videoPickerUris != null && videoView1 != null){
            videoView1.start();
        }
    }

    class WorkerUsingThread implements Runnable {
        private volatile boolean running = false;
        private Thread thread;
        private Activity activity;

        WorkerUsingThread(Activity activity) {
            this.activity = activity;
        }

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
            String retval;
            Runalyzer runalyzer = new Runalyzer(inputVideoUris, millisCreationTime);
            if(running){
                Log.d("Benni", "millisCreationTime[0] = " + millisCreationTime[0]);
                Log.d("Benni", "millisCreationTime[1] = " + millisCreationTime[1]);
                print("Detecting runner-information...");
                retval = runalyzer.detectRunnerInformation(Activity_CreateCompilation.this);
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Detecting maximum runner-width and -height...");
                retval = runalyzer.detectRunnerDimensions();
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Cropping all single frames...");
                retval = runalyzer.cropSingleFrames();
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Creating final video...");
                retval = runalyzer.createFinalVideo();
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

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
                        Log.d("Benni", "Before Join");
                        thread.join();
                        Log.d("Benni", "Thread stopped");
                        break;
                    } catch (Exception e) {
                        Log.d("Benni", "Error stopping thread");
                    }
                }
                String filePath = FilePathHolder.getInstance().getFilePath();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("filePath", filePath);
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        }
    }


}