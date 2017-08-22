package com.android.miniexplorer;

import android.content.Intent;
import android.hardware.Sensor;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;

import com.android.miniexplorer.Handler.IpAddressHandler;
import com.android.miniexplorer.Handler.SocketHandler;
import com.github.nisrulz.sensey.RotationAngleDetector;
import com.github.nisrulz.sensey.Sensey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class VRActivity extends AppCompatActivity {

    WebView webView;
    ImageView leftTurnLeft;
    ImageView leftTurnRight;
    ImageView rightTurnLeft;
    ImageView rightTurnRight;

    RotationAngleDetector.RotationAngleListener rotationAngleListener;
    boolean initial = true;
    int counter = 0;
    int params = 0;
    double Amin = 0;
    double Amax = 0;
    double Bmin = 0;
    double Bmax = 180;
    double B = 0;
    int signalTurn = 0;


    JSONObject object;
    final String LOG_TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr);

        Utilities.setFullScreen(getWindow());

        leftTurnLeft = (ImageView) findViewById(R.id.left_turnleft);
        leftTurnRight = (ImageView) findViewById(R.id.left_turnright);
        rightTurnLeft = (ImageView) findViewById(R.id.right_turnleft);
        rightTurnRight = (ImageView) findViewById(R.id.right_turnright);


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

        final Socket socket = SocketHandler.getSocket();
        if (socket != null) {
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String response = null;
                        while ((response = in.readLine()) != null) {
                            try {
                                object = new JSONObject(response);
                                signalTurn = object.getInt("led");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (signalTurn == 0) {
                                            leftTurnLeft.setVisibility(View.INVISIBLE);
                                            leftTurnRight.setVisibility(View.INVISIBLE);
                                            rightTurnLeft.setVisibility(View.INVISIBLE);
                                            rightTurnRight.setVisibility(View.INVISIBLE);
                                        } else if (signalTurn == 1) {
                                            leftTurnLeft.setVisibility(View.VISIBLE);
                                            leftTurnRight.setVisibility(View.INVISIBLE);
                                            rightTurnLeft.setVisibility(View.VISIBLE);
                                            rightTurnRight.setVisibility(View.INVISIBLE);
                                        } else {
                                            leftTurnLeft.setVisibility(View.INVISIBLE);
                                            leftTurnRight.setVisibility(View.VISIBLE);
                                            rightTurnLeft.setVisibility(View.INVISIBLE);
                                            rightTurnRight.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            } catch (JSONException ex) {
                                Log.e(LOG_TAG, ex.toString());
                            }
                        }
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, ex.toString());
                    }
                }
            });
//            readThread.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().startRotationAngleDetection(rotationAngleListener);
        Sensey.getInstance().stop();
    }
}
