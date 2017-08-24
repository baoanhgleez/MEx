package com.android.miniexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.miniexplorer.Handler.IpAddressHandler;
import com.android.miniexplorer.Handler.SocketHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    //Declare view component
    RelativeLayout controlLayout;
    Button btnSteeringWheel;
    Button btnSpeed;
    Button btnBrake;
    Button btnGearSwitch;
    Button btnSpeaker;
    Button btnSignalLeft;
    Button btnSignalRight;
    Button btnVr;
    ImageButton btnDisconnect;
    WebView webView;

    GestureDetector gestureDetector;
    Thread sendThread;

    //variables
    double gearSwitchMidPoint;
    boolean isContinuous = true;

    JSONObject data = new JSONObject();
    int buzzer = 0;
    int vr = 0;
    static int mode = 0;
    static double vrRotateAngle = 90;
    static int SPEED_LEVEL_MIN = 0;
    static int SPEED_LEVEL = 0;
    static int SPEED_LEVEL_MAX = 100;

    boolean doubleBackToExit = false;

    final String LOG_TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initial_views();
        sendData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vr = 0;
        resetSavedValue();

        Utilities.setFullScreen(getWindow());
        if (IpAddressHandler.getServerIpAddress() != null && !IpAddressHandler.getServerIpAddress().isEmpty()) {
            webView.loadUrl("http://" + IpAddressHandler.getServerIpAddress() + ":" + Utilities.ANDROID_VR_PORT + "/" + Utilities.STREAM_NORMAL_URL);
        } else {
            webView.loadUrl("about:blank");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.loadUrl("about:blank");
    }

    @Override
    protected void onStop() {
        super.onStop();
        webView.loadUrl("about:blank");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isContinuous = false;
    }

    private void initial_views() {
        controlLayout = (RelativeLayout) findViewById(R.id.controlLayout);

        btnSteeringWheel = (Button) findViewById(R.id.btnStreeringWheel);
        btnSpeed = (Button) findViewById(R.id.btnSpeed);
        btnBrake = (Button) findViewById(R.id.btnBrake);
        btnGearSwitch = (Button) findViewById(R.id.btnGearSwitcher);
        btnSpeaker = (Button) findViewById(R.id.btnSpeaker);
        btnSignalLeft = (Button) findViewById(R.id.btnTurnLeft);
        btnSignalRight = (Button) findViewById(R.id.btnTurnRight);
        btnVr = (Button) findViewById(R.id.btnVr);
        btnDisconnect = (ImageButton) findViewById(R.id.btnDisconnect);
        webView = (WebView) findViewById(R.id.webView);

        // set event handler
        btnSteeringWheel.setOnTouchListener(this);
        btnSpeed.setOnTouchListener(this);
        btnBrake.setOnTouchListener(this);
        btnGearSwitch.setOnTouchListener(this);
        btnSpeaker.setOnTouchListener(this);
        btnSignalRight.setOnTouchListener(this);
        btnSignalLeft.setOnTouchListener(this);
        btnVr.setOnTouchListener(this);
        btnDisconnect.setOnTouchListener(this);

        //Gesture detector
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    gearSwitchMidPoint = btnGearSwitch.getY();
                    if (e.getY() <= gearSwitchMidPoint) {
                        if (mode > 0) {
                            mode -= 1;
                            SPEED_LEVEL = 10;
                        }
                    } else if (e.getY() > gearSwitchMidPoint) {
                        if (mode < 2) {
                            mode += 1;
                            SPEED_LEVEL = 10;
                        }
                    }
                    if (mode == 0) {
                        btnGearSwitch.setBackgroundResource(R.drawable.parkingmode);
                    }
                    if (mode == 1) {
                        btnGearSwitch.setBackgroundResource(R.drawable.drivemode);
                    }
                    if (mode == 2) {
                        btnGearSwitch.setBackgroundResource(R.drawable.reversemode);
                    }
                }
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                switch (getSlope(e1.getX(), e1.getY(), e2.getX(), e2.getY())) {
                    case 1:
                        if (mode > 0) {
                            mode -= 1;
                            SPEED_LEVEL = 10;
                        }
                        break;
                    case 3:
                        if (mode < 2) {
                            mode += 1;
                            SPEED_LEVEL = 10;
                        }
                        break;
                }
                if (mode == 0) {
                    btnGearSwitch.setBackgroundResource(R.drawable.parkingmode);
                }
                if (mode == 1) {
                    btnGearSwitch.setBackgroundResource(R.drawable.drivemode);
                }
                if (mode == 2) {
                    btnGearSwitch.setBackgroundResource(R.drawable.reversemode);
                }
                return true;
            }
        });
    }

    public void sendData() {
        isContinuous = true;

        if (sendThread == null) {
            sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket socket = SocketHandler.getSocket();
                    DataOutputStream out = null;
                    BufferedReader in = null;
                    try {
                        out = new DataOutputStream(socket.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        while (isContinuous) {
                            data = new JSONObject();
                            try {
                                if (vr == 0) {
                                    data.put("vr", vr);
                                    data.put("mode", mode);
                                    data.put("speed", SPEED_LEVEL);
                                    data.put("angle", Math.round(rotateAngle + 180));
                                    data.put("buzzer", buzzer);
                                    data.put("led", signalTurnFlag);
                                } else if (vr == 1) {
                                    data.put("vr", vr);
                                    data.put("angle", vrRotateAngle);
                                }
                            } catch (JSONException e) {
                                Log.e(LOG_TAG, e.toString());
                            }
                            out.writeBytes(data.toString());
                            out.flush();
                            Thread.sleep(100);
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                            if (socket != null) {
                                socket.close();
                            }
                        } catch (IOException ex) {
                            Log.e(getClass().getName(), ex.toString());
                        }
                    }
                }
            });
            sendThread.start();
        }
    }

    @Override
    public boolean onTouch(View widget, MotionEvent event) {
        int id = widget.getId();

        switch (id) {
            case R.id.btnStreeringWheel:
                onWheelRotate(event);
                break;

            case R.id.btnBrake:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnBrake.setBackgroundResource(R.drawable.leftpedalpressed);
                        if (mode != 0) {
                            if (SPEED_LEVEL > SPEED_LEVEL_MIN) {
                                SPEED_LEVEL -= 10;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        btnBrake.setBackgroundResource(R.drawable.leftpedal);
                        break;
                }
                break;
            case R.id.btnSpeed:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnSpeed.setBackgroundResource(R.drawable.rightpedalpressed);
                        if (mode != 0) {
                            if (SPEED_LEVEL < SPEED_LEVEL_MAX) {
                                SPEED_LEVEL += 10;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        btnSpeed.setBackgroundResource(R.drawable.rightpedal);
                        break;
                }
                break;

            case R.id.btnSpeaker:
                soundOn(event);
                break;

            case R.id.btnTurnLeft:
            case R.id.btnTurnRight:
                onSignalTurn(id, event.getAction());
                break;

            case R.id.btnGearSwitcher:
                gestureDetector.onTouchEvent(event);
                break;

            case R.id.btnDisconnect:
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        isContinuous = false;
                        Intent intentCheckActivity = new Intent(getApplicationContext(), CheckConnectionActivity.class);
                        startActivity(intentCheckActivity);
                        finish();
                        break;
                }
                break;
            case R.id.btnVr:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Intent intentVrActivity = new Intent(this, VRActivity.class);
                        vr = 1;
                        webView.loadUrl("about:blank");
                        startActivity(intentVrActivity);
                        break;
                }
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExit) {
            super.onBackPressed();
            return;
        }

        doubleBackToExit = true;
        Utilities.makeToast(getApplicationContext(), "Press back again to leave");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 2000);
    }

    int signalTurnFlag;

    /**
     * Control the led to send signal turn
     * signalTurnFlag: Save the instance of signal button
     * 0 : No signal
     * 1 : Turn left
     * 2 : Turn right
     *
     * @param id     of the view which was pressed
     * @param action action of finger
     */
    private void onSignalTurn(int id, int action) {
        if (action == MotionEvent.ACTION_UP) {

            if (id == R.id.btnTurnLeft) {
                // if the left-led is on, turn it off.
                if (signalTurnFlag == 1) {
                    signalTurnFlag = 0;
                } else {
                    signalTurnFlag = 1;
                }
            } else { // do the same for right-led
                if (signalTurnFlag == 2) {
                    signalTurnFlag = 0;
                } else {
                    signalTurnFlag = 2;
                }
            }

            if (signalTurnFlag == 0) {
                btnSignalLeft.setBackgroundResource(R.drawable.turnleft);
                btnSignalRight.setBackgroundResource(R.drawable.turnrigh);
            } else if (signalTurnFlag == 1) {
                btnSignalLeft.setBackgroundResource(R.drawable.turnlefton);
                btnSignalRight.setBackgroundResource(R.drawable.turnrigh);
            } else {
                btnSignalLeft.setBackgroundResource(R.drawable.turnleft);
                btnSignalRight.setBackgroundResource(R.drawable.turnrighon);
            }
        }
    }

    private void soundOn(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                buzzer = 1;
                break;
            case MotionEvent.ACTION_UP:
                buzzer = 0;
                break;
        }
    }


    double viewRotation, fingerRotation, newFingerRotation;
    float rotateAngle;

    /**
     * Control and get rotation of steering wheel
     *
     * @param event save position of finger to calculate the angle
     */
    private void onWheelRotate(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float xc = btnSteeringWheel.getWidth() / 2;
        final float yc = btnSteeringWheel.getHeight() / 2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                fingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                break;
            case MotionEvent.ACTION_MOVE:
                newFingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                if (Math.abs(newFingerRotation - fingerRotation) < 90.0f)
                    rotateAngle = (float) ((newFingerRotation - fingerRotation));
                btnSteeringWheel.setRotation(rotateAngle);
                break;
            case MotionEvent.ACTION_UP:
                fingerRotation = newFingerRotation = rotateAngle = 0.0f;
                btnSteeringWheel.setRotation(rotateAngle);
                break;
        }
    }


    /**
     * Reset values for next execution;
     */
    private void resetSavedValue() {
        signalTurnFlag = 0;
    }

    private int getSlope(float x1, float y1, float x2, float y2) {
        Double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));
        if (angle > 45 && angle <= 135)
            // top
            return 1;
        if (angle >= 135 && angle < 180 || angle < -135 && angle > -180)
            // left
            return 2;
        if (angle < -45 && angle >= -135)
            // down
            return 3;
        if (angle > -45 && angle <= 45)
            // right
            return 4;
        return 0;
    }


}