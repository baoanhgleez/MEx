package com.android.miniexplorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebView;

import com.android.miniexplorer.Handler.IpAddressHandler;
import com.github.nisrulz.sensey.RotationAngleDetector;
import com.github.nisrulz.sensey.Sensey;

public class VRCardBoard extends AppCompatActivity {

    WebView webView;
    RotationAngleDetector.RotationAngleListener rotationAngleListener;
    boolean initial = true;
    int counter = 0;
    int params = 0;
    double Amin = 0;
    double Amax = 0;
    double Bmin = 0;
    double Bmax = 180;
    double B = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrcard_board);
        Sensey.getInstance().init(getApplicationContext());
        rotationAngleListener = new RotationAngleDetector.RotationAngleListener() {
            @Override
            public void onRotation(float v, float v1, float v2) {
                if (initial) {
                    params += v;
                    if (counter == 10) {
                        initial = false;
                        int abc = params / 10;
                        Amin = abc - 90;
                        Amax = abc + 90;
                    }
                    counter += 1;
                } else {
                    B = (v-Amin)/(Amax-Amin) * (Bmax-Bmin) + Bmin;
                    if (B < 0) {
                        B = 0;
                    } else if (B > 180) {
                        B = 180;
                    }
                    MainActivity.vrRotateAngle = Math.round(B);
                }
            }
        };
        Sensey.getInstance().startRotationAngleDetection(rotationAngleListener);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        webView = (WebView) findViewById(R.id.webView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.setFullScreen(getWindow());
        webView.loadUrl("http://" + IpAddressHandler.getServerIpAddress() + ":" + Utilities.ANDROID_VR_PORT + "/" + Utilities.STREAMING_VR_URL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        webView.loadUrl("about:blank");
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.loadUrl("about:blank");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
