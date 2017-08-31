package com.android.miniexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.nisrulz.sensey.Sensey;

public class SplashActivity extends AppCompatActivity {

    TextView txtCountDownLeft;
    TextView txtCountDownRight;
    Button btnStart;

    Handler handler;

    int count = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        txtCountDownLeft = (TextView) findViewById(R.id.txtCountDownLeft);
        txtCountDownRight = (TextView) findViewById(R.id.txtCountDownRight);
        btnStart = (Button) findViewById(R.id.btnStart);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnStart.setVisibility(View.GONE);
                txtCountDownLeft.setVisibility(View.VISIBLE);
                txtCountDownLeft.setText(String.valueOf(count));
                txtCountDownRight.setVisibility(View.VISIBLE);
                txtCountDownRight.setText(String.valueOf(count));
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       count -= 1;
                        txtCountDownLeft.setText(String.valueOf(count));
                        txtCountDownRight.setText(String.valueOf(count));
                        if (count == 0) {
                            Intent intent = new Intent(getApplicationContext(), VRActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        handler.postDelayed(this, 1000);
                    }
                }, 1000);

            }
        });
    }
}
