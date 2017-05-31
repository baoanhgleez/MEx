package com.android.miniexplorer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.vr.sdk.base.HeadTransform;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    RelativeLayout controlLayout;

    Button btnSteeringWheel, btnSpeed, btnBrake, btnGearSwitch;
    Button btnSignalLeft, btnSignalRight, btnSpeaker;
    Button btnConnect;
    EditText edtAddress, edtPort;

    RelativeLayout infoLayout;
    ImageView ledSignalLeft, ledSignalRight;
    TextView txSpeed;

    TextView txtPitch, txtYaw, txtRoll;

    JSONObject json = new JSONObject();
    JSONObject angleNode = new JSONObject();
    JSONObject speedNode = new JSONObject();

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

        btnConnect = (Button) findViewById(R.id.btnConnect);
        edtAddress = (EditText) findViewById(R.id.edtAddress);
        edtPort = (EditText) findViewById(R.id.edtPort);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client myClient = new Client(edtAddress.getText().toString(), Integer.parseInt(edtPort.getText().toString()), speedNode);
                myClient.execute();
            }
        });

        //Demo
        txtPitch = (TextView) findViewById(R.id.pitch);
        txtYaw = (TextView) findViewById(R.id.yaw);
        txtRoll = (TextView) findViewById(R.id.roll);
        float[] abc = new float[4];
        HeadTransform headTransform = new HeadTransform();
        headTransform.getQuaternion(abc, 0);
        txtPitch.setText(String.valueOf(abc[0]));
        txtYaw.setText(String.valueOf(abc[1]));
        txtRoll.setText(String.valueOf(abc[2]));
    }

    @Override
    public boolean onTouch(View widget, MotionEvent event) {
        int id = widget.getId();

        switch (id){
            case R.id.btnStreeringWheel:
                onWheelRotate(event);
                break;

            case R.id.btnBrake:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnBrake.setBackgroundResource(R.drawable.leftpedalpressed);
                        try {
                            speedNode.put("value", "BACKWARD");
                            txtRoll.setText(speedNode.get("value").toString());
                        } catch (JSONException e) {
                            Log.e("ERROR", e.getMessage());
                        }
                        //Brake
                        //btnBrake.setBackground(getDrawable(R.drawable.rightpedalpressed));
                        break;
                    case MotionEvent.ACTION_UP:
                        btnBrake.setBackgroundResource(R.drawable.leftpedal);
                        //btnBrake.setBackground(getDrawable(R.drawable.rightpedal));
                        break;
                }
                break;
            case R.id.btnSpeed:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnSpeed.setBackgroundResource(R.drawable.rightpedalpressed);
                        try {
                            speedNode.put("value", "FORWARD");
                            txtRoll.setText(speedNode.get("value").toString());
                        } catch (JSONException e) {
                            Log.e("ERROR", e.getMessage());
                        }
                        //Forward
                        //btnSpeed.setBackground(getDrawable(R.drawable.rightpedalpressed));
                        break;
                    case MotionEvent.ACTION_UP:
                        btnSpeed.setBackgroundResource(R.drawable.rightpedal);
                        //btnSpeed.setBackground(getDrawable(R.drawable.rightpedal));
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
        }
        return false;
    }

    private void onPedalPress(int id, MotionEvent event) {
        switch (id) {
            case R.id.btnSpeed:
                break;
            case R.id.btnBrake:
                break;
        }
    }



    int signalTurnFlag;

    /**Control the led to send signal turn
     * signalTurnFlag: Save the instance of signal button
     *      0 : No signal
     *      1 : Turn left
     *      2 : Turn right
     * @param id of the view which was pressed
     * @param action action of finger
     */
    private void onSignalTurn(int id, int action) {
        if (action==MotionEvent.ACTION_UP) {

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

    /** Control and get rotation of steering wheel
     * @param event save position of finger to calculate the angle
     */
    private void onWheelRotate(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        final float xc = btnSteeringWheel.getWidth()/2;
        final float yc = btnSteeringWheel.getHeight()/2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                viewRotation = btnSteeringWheel.getRotation();
                fingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                break;
            case MotionEvent.ACTION_MOVE:
                newFingerRotation = Math.toDegrees(Math.atan2(x - xc, yc - y));
                rotateAngle = (float)(viewRotation + newFingerRotation - fingerRotation);
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

}