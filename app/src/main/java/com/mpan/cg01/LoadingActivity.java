package com.mpan.cg01;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    private static final long DELAY_DURATION = 3650;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        VideoView videoView = findViewById(R.id.videoView);
        ProgressBar spinner = findViewById(R.id.progressBar1);

        // Set the video URI and start playing
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loading);
        videoView.setVideoURI(videoUri);
        videoView.start();

        HandlerThread handlerThread = new HandlerThread("LoadingThread");
        handlerThread.start();

        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Hide the progress bar
                        spinner.setVisibility(View.GONE);

                        // Show the video view
                        videoView.setVisibility(View.VISIBLE);

                        // Set a listener to start the next activity once the video finishes
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                Intent intent = new Intent(LoadingActivity.this, GameOptionsActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
            }
        }, DELAY_DURATION);
    }
}
