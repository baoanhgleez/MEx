package com.android.miniexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.miniexplorer.Handler.IpAddressHandler;
import com.android.miniexplorer.Handler.SocketHandler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class CheckConnectionActivity extends Activity implements Serializable {

    final String LOG_TAG = getClass().getName();

    TextView txtMessage;
    TextView txtDeviceModel;
    ProgressBar progressBar;
    Button btnConnect;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_connection);

        initializeComponent();
    }

    private void initializeComponent() {
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        txtDeviceModel = (TextView) findViewById(R.id.txtDeviceModel);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnConnect = (Button) findViewById(R.id.btnConnect);

        //Set custom font
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");
        txtMessage.setTypeface(customFont);
        btnConnect.setTypeface(customFont);
        customFont = Typeface.createFromAsset(getAssets(), "fonts/Lato-LightItalic.ttf");
        txtDeviceModel.setTypeface(customFont);


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preProgress();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkConnection();
                    }
                }, 500);
            }
        });
    }

    private void checkConnection() {
        //Check wifi state
        if (wifiManager.isWifiEnabled() == false) {
            noWifiNotify();
            return;
        }
        String deviceIpAddress = getIpAddress();
        if (deviceIpAddress != null && !deviceIpAddress.isEmpty()) {
            final String serverIpAddress = getServerIpAddress(deviceIpAddress);
            if (serverIpAddress != null && !serverIpAddress.isEmpty()) {
                IpAddressHandler.setServerIpAddress(serverIpAddress);
                IpAddressHandler.setDeviceIpAddress(deviceIpAddress);

                Thread connect = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Socket socket = null;
                        DataOutputStream out = null;
                        BufferedReader in = null;
                        try {
                            socket = new Socket(serverIpAddress, Utilities.ANDROID_CONTROL_PORT);
                            socket.setSoTimeout(5000);
                            out = new DataOutputStream(socket.getOutputStream());
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            //Send authentication code
                            out.writeBytes("ANDROID");
                            out.flush();

                            String response;
                            while ((response = in.readLine()) != null) {
                                if (response.contains("OK")) {
                                    SocketHandler.setSocket(socket);
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                if (response.contains("LIMIT")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            overCapacityNotify();
                                        }
                                    });
                                }
                                if (response.contains("NO")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            noConnectionNotify();
                                        }
                                    });
                                }
                            }
                        } catch (SocketTimeoutException ex) {
                            Log.e(LOG_TAG, ex.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noConnectionNotify();
                                }
                            });
                        } catch (IOException ex) {
                            Log.e(LOG_TAG, ex.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noConnectionNotify();
                                }
                            });
                        }
                    }
                });
                connect.start();
            }
        } else {
            noConnectionNotify();
        }
    }

    private void noWifiNotify() {
        txtMessage.setText(getString(R.string.no_wifi));
        endProgress();
    }

    private void noConnectionNotify() {
        txtMessage.setText(getString(R.string.no_connection));
        endProgress();
    }

    private void overCapacityNotify() {
        txtMessage.setText(getString(R.string.over_capacity));
        endProgress();
    }

    private void preProgress() {
        btnConnect.setEnabled(false);
        if (txtMessage.getVisibility() == View.VISIBLE) {
            txtMessage.setVisibility(View.INVISIBLE);
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    private void endProgress() {
        btnConnect.setEnabled(true);
        if (txtMessage.getText() != null && !txtMessage.getText().toString().isEmpty()) {
            txtMessage.setVisibility(View.VISIBLE);
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    //Get network ip address
    private String getIpAddress() {
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();
        String ipString;
        try {
            ipString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(LOG_TAG, "Unable to get host address.");
            ipString = null;
        }
        return ipString;
    }

    //Change the last number to 1 to get socket server's address
    private String getServerIpAddress(String ipAddress) {
        StringBuilder serverIpAddress = new StringBuilder();
        if (ipAddress != null && !ipAddress.isEmpty()) {
            String[] ipArray = ipAddress.split("\\.");
            for (int i = 0; i < ipArray.length; i++) {
                if (i == ipArray.length - 1) {
                    ipArray[i] = "1";
                }
                serverIpAddress.append(ipArray[i]);
                if (i != ipArray.length - 1) {
                    serverIpAddress.append(".");
                }
            }
        }
        return serverIpAddress.toString();
    }
}
