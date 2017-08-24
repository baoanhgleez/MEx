package com.android.miniexplorer;

import android.content.Intent;
import android.hardware.Sensor;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.TimerTask;

public class VRActivity extends AppCompatActivity {

    WebView webView;
    ImageView leftTurnLeft;
    ImageView leftTurnRight;
    ImageView rightTurnLeft;
    ImageView rightTurnRight;

    boolean initial = true;
    double startCoordinate;

    boolean isContinuous = true;
    int signalTurn = 0;

    RotationAngleDetector.RotationAngleListener rotationAngleListener;
    Socket socket;
    BufferedReader in;
    Thread readThread;
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
                    startCoordinate = v;
                    initial = false;
                } else {
                    double rotateAngle = (v - startCoordinate + 540) % 360 - 180;
                    double angle = 90 + rotateAngle;
                    if (angle < 0) {
                        angle = 0;
                    } else if (angle > 180) {
                        angle = 180;
                    }
                    MainActivity.vrRotateAngle = Math.round(angle);
                }
            }
        };

        socket = SocketHandler.getSocket();
        if (socket != null) {
            readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String response = null;
                        while ((response = in.readLine()) != null && isContinuous) {
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
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                            Log.e(LOG_TAG, ex.toString());
                        }

                    }
                }
            });
            readThread.start();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        webView = (WebView) findViewById(R.id.webView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensey.getInstance().startRotationAngleDetection(rotationAngleListener);
        if (IpAddressHandler.getServerIpAddress() != null) {
            webView.loadUrl("http://" + IpAddressHandler.getServerIpAddress() + ":" + Utilities.ANDROID_VR_PORT + "/" + Utilities.STREAMING_VR_URL);
        } else {
            webView.loadUrl("about:blank");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        webView.loadUrl("about:blank");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Sensey.getInstance().stopRotationAngleDetection(rotationAngleListener);
        MainActivity.vrRotateAngle = 90;
        webView.loadUrl("about:blank");
    }

    @Override
    protected void onDestroy() {
        webView.loadUrl("about:blank");
        isContinuous = false;
        Sensey.getInstance().stopRotationAngleDetection(rotationAngleListener);
        Sensey.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
