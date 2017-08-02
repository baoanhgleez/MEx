package com.android.miniexplorer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    //Declare variables
    RelativeLayout controlLayout;
    Button btnSteeringWheel, btnSpeed, btnBrake, btnGearSwitch;
    Button btnSignalLeft, btnSignalRight, btnSpeaker;
    RelativeLayout infoLayout;
    ImageView ledSignalLeft, ledSignalRight;
    TextView txSpeed;

    GestureDetector gestureDetector;
    boolean flag = true;
    JSONObject data = new JSONObject();
    static int mode = 0;
    static int SPEED_LEVEL_MIN = 0;
    static int SPEED_LEVEL = 0;
    static int SPEED_LEVEL_MAX = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(Utilities.TAG, "onCreate() start");

        super.onCreate(savedInstanceState);

        // hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initial_views();
        resetSavedValue();

        Log.i(Utilities.TAG, "onCreate() end");
        sendData();
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

        // set event handler
        btnSteeringWheel.setOnTouchListener(this);
        btnSpeed.setOnTouchListener(this);
        btnBrake.setOnTouchListener(this);
        btnGearSwitch.setOnTouchListener(this);

        btnSpeaker.setOnTouchListener(this);
        btnSignalRight.setOnTouchListener(this);
        btnSignalLeft.setOnTouchListener(this);

        infoLayout = (RelativeLayout) findViewById(R.id.infoLayout);
        ledSignalLeft = (ImageView) findViewById(R.id.ledTurnLeft);
        ledSignalRight = (ImageView) findViewById(R.id.ledTurnRight);
        txSpeed = (TextView) findViewById(R.id.txSpeed);

        // turn off leds
        ledSignalLeft.setVisibility(ImageView.GONE);
        ledSignalRight.setVisibility(ImageView.GONE);

        //gesture detector
        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
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
                            SPEED_LEVEL = 0;
                        }
                        break;
                    case 3:
                        if (mode < 2) {
                            SPEED_LEVEL = 0;
                            mode += 1;
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
        flag = true;
        Thread threadSend = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = SocketHandler.getSocket();
                DataOutputStream out = null;
                BufferedReader in = null;
                try {
                        out = new DataOutputStream(socket.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        while (flag) {
                            try {
                                data.put("mode", mode);
                                data.put("speed", SPEED_LEVEL);
                                data.put("angle", Math.round(rotateAngle + 180));
                                //xinhan
                                //speed gui ve
                                //headtracker
                                Log.d("GIA TRI", data.toString());
                            } catch (JSONException e) {
                                Log.e("ERROR", e.getMessage());
                            }
                            out.writeBytes(data.toString());
                            out.flush();
                            Thread.sleep(100);
                        }

                        /*Todo Sửa lại luồng làm việc của thread gửi/nhận hiện giờ chỉ gửi data khi nhận được response ok*/
                        /*Todo Sửa lại việc kết nối: Ý tưởng là socket (t1 CheckConnectionActivity) -> MainActivity -> t2(socket)*/
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
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
                            flag = false;
                            socket.close();
                        }
                    } catch (IOException ex) {
                        Log.e(getClass().getName(), ex.toString());
                    }
                }
            }
        });
        threadSend.start();
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
                                SPEED_LEVEL -= 2;
                                txSpeed.setText(String.valueOf(SPEED_LEVEL));
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
                                SPEED_LEVEL += 2;
                                txSpeed.setText(String.valueOf(SPEED_LEVEL));
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        btnSpeed.setBackgroundResource(R.drawable.rightpedal);
                        break;
                }
                break;

            case R.id.btnSpeaker:
                soundOn();
                break;

            case R.id.btnTurnLeft:
            case R.id.btnTurnRight:
                onSignalTurn(id, event.getAction());
                break;

            case R.id.btnGearSwitcher:
                gestureDetector.onTouchEvent(event);
                break;
        }
        return false;
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
                // if the left-led is turning on, turn of signal.
                if (signalTurnFlag == 1) {
                    signalTurnFlag = 0;
                    ledSignalLeft.setVisibility(ImageView.GONE);
                } else {
                    if (signalTurnFlag == 2) { // turn off right-led if it's on
                        ledSignalRight.setVisibility(ImageView.GONE);
                    }
                    signalTurnFlag = 1;
                    ledSignalLeft.setVisibility(ImageView.VISIBLE);

                }
            } else { // do the same for right-led
                if (signalTurnFlag == 2) {
                    signalTurnFlag = 0;
                    ledSignalRight.setVisibility(ImageView.GONE);
                } else {
                    if (signalTurnFlag == 1) {
                        ledSignalLeft.setVisibility(ImageView.GONE);
                    }
                    signalTurnFlag = 2;
                    ledSignalRight.setVisibility(ImageView.VISIBLE);
                }
            }
        }
    }

    private void soundOn() {
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
                viewRotation = btnSteeringWheel.getRotation();
                fingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                break;
            case MotionEvent.ACTION_MOVE:
                newFingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                if (Math.abs((viewRotation + newFingerRotation - fingerRotation)) <= 90)
                    rotateAngle = (float) (viewRotation + newFingerRotation - fingerRotation);
                btnSteeringWheel.setRotation(rotateAngle);
                break;
            case MotionEvent.ACTION_UP:
                fingerRotation = newFingerRotation = rotateAngle = 0.0f;
                try {
                    data.put("angle", Math.round(rotateAngle + 180));
                } catch (JSONException e) {
                    Log.e("ERROR", e.getMessage());
                }
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
        if (angle < -45 && angle>= -135)
            // down
            return 3;
        if (angle > -45 && angle <= 45)
            // right
            return 4;
        return 0;
    }
}