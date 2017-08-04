package com.android.miniexplorer;

import android.view.View;
import android.view.Window;

/**
 * Created by luyen on 21/05/2017.
 */

public class Utilities {
    public static final String TAG = "MEx";
    /**
     * URL Stream
     */
    public static final String STREAMING_VR_URL = "http://192.168.0.1:8012/vr";
    public static final String STREAM_NORMAL_URL = "http://192.168.0.1:8012/normal";
    public static final String VR_CARD_BOARD_INTENT = "android.intent.action.LAUCHER_VR_MODE";

    public static void setFullScreen(Window windows){
        windows.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
    public static final int ANDROID_PORT = 8011;
}
