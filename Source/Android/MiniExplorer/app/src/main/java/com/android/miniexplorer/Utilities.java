package com.android.miniexplorer;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

/**
 * Created by luyen on 21/05/2017.
 */

public class Utilities {
    public static final String TAG = "MEx";
    public static final int ANDROID_CONTROL_PORT = 8011;
    public static final int ANDROID_VR_PORT = 8012;

    public static final String STREAMING_VR_URL = "vr";
    public static final String STREAM_NORMAL_URL = "normal";

    public static void setFullScreen(Window windows) {
        windows.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    public static double mapping(double value, double aMin, double aMax, double bMin, double bMax) {
        double EPSILON = 1e-12;
        if (Math.abs(aMax - aMin) < EPSILON) {
            throw new ArithmeticException("CANNOT DIVIDE BY ZERO");
        }
        double result;
        result = (value - aMin) / (aMax - aMin) * (bMax - bMin) + bMin;
        return result;
    }

    public static void makeToast(Context context, String string) {
        if (string != null && !string.isEmpty()) {
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        }
    }

}
