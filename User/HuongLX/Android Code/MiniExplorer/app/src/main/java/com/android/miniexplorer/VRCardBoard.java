package com.android.miniexplorer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

public class VRCardBoard extends Activity {

    protected VrVideoView videoWidgetView;
    VideoLoaderTask backgroundVideoLoaderTask;
    Uri fileUri;
    VrVideoView.Options videoOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vrcard_board);
        videoWidgetView = (VrVideoView) findViewById(R.id.video_view);
        videoWidgetView.setEventListener(new VrVideoEventListener());
        try {
            VrVideoView.Options options = new VrVideoView.Options();
            options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
            videoWidgetView.loadVideoFromAsset("congo.mp4", options);
            Toast.makeText(this,"video is running",Toast.LENGTH_SHORT).show();
        }catch (Exception ex){
            Toast.makeText(this,"Fail to load video",Toast.LENGTH_SHORT).show();
        }
        //handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i("ERROR", "ACTION_VIEW Intent received");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.w("ERROR", "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i("ERROR", "Using file " + fileUri.toString());
            }

            videoOptions.inputFormat = intent.getIntExtra("inputFormat", VrVideoView.Options.FORMAT_DEFAULT);
            videoOptions.inputType = intent.getIntExtra("inputType", VrVideoView.Options.TYPE_MONO);
        } else {
            Log.i("ERROR", "Intent is not ACTION_VIEW. Using the default video.");
            fileUri = null;
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.

        if (backgroundVideoLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundVideoLoaderTask.cancel(true);
        }
        backgroundVideoLoaderTask = new VideoLoaderTask();
        backgroundVideoLoaderTask.execute(Pair.create(fileUri, videoOptions));
    }
    class VideoLoaderTask extends AsyncTask<Pair<Uri, VrVideoView.Options>, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Pair<Uri, VrVideoView.Options>... fileInformation) {
            try {
                if (fileInformation == null || fileInformation.length < 1
                        || fileInformation[0] == null || fileInformation[0].first == null) {
                    // No intent was specified, so we default to playing the local stereo-over-under video.
                    VrVideoView.Options options = new VrVideoView.Options();
                    options.inputType = VrVideoView.Options.TYPE_STEREO_OVER_UNDER;
                    videoWidgetView.loadVideoFromAsset("congo.mp4", options);
                } else {
                    videoWidgetView.loadVideo(fileInformation[0].first, fileInformation[0].second);
                }
            } catch (IOException e) {
                // An error here is normally due to being unable to locate the file.
//                loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
                // Since this is a background thread, we need to switch to the main thread to show a toast.
                videoWidgetView.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast
                                .makeText(VRCardBoard.this, "Error opening file. ", Toast.LENGTH_LONG)
                                .show();
                    }
                });
                Log.e("ERROR: ", "Could not open video: " + e);
            }

            return true;
        }
    }
//    private class ActivityEventListener extends VrVideoEventListener {
//        /**
//         * Called by video widget on the UI thread when it's done loading the video.
//         */
//        @Override
//        public void onLoadSuccess() {
//            Log.i(TAG, "Successfully loaded video " + videoWidgetView.getDuration());
//            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
//            seekBar.setMax((int) videoWidgetView.getDuration());
//            updateStatusText();
//        }
//
//        /**
//         * Called by video widget on the UI thread on any asynchronous error.
//         */
//        @Override
//        public void onLoadError(String errorMessage) {
//            // An error here is normally due to being unable to decode the video format.
//            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
//            Toast.makeText(
//                    SimpleVrVideoActivity.this, "Error loading video: " + errorMessage, Toast.LENGTH_LONG)
//                    .show();
//            Log.e(TAG, "Error loading video: " + errorMessage);
//        }
//
//        @Override
//        public void onClick() {
//            togglePause();
//        }
//
//        /**
//         * Update the UI every frame.
//         */
//        @Override
//        public void onNewFrame() {
//            updateStatusText();
//            seekBar.setProgress((int) videoWidgetView.getCurrentPosition());
//        }
//
//        /**
//         * Make the video play in a loop. This method could also be used to move to the next video in
//         * a playlist.
//         */
//        @Override
//        public void onCompletion() {
//            videoWidgetView.seekTo(0);
//        }
//    }
}