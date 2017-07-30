package com.android.miniexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class CheckConnectionActivity extends Activity {

    TextView txtMessage;
    ImageView imgMessage;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_connection);

        initComponent();
        checkConnection();
    }

    private void checkConnection() {
        if (wifiManager.isWifiEnabled() == false) {
            txtMessage.setText(getResources().getString(R.string.no_wifi));
            imgMessage.setImageResource(R.drawable.icons8_offline);
        } else {
            String ipString = getIpAddress();
            if (ipString != null && !ipString.isEmpty()) {
                txtMessage.setText(getResources().getString(R.string.no_connection));
                imgMessage.setImageResource(R.drawable.icons8_offline);
                final String serverIpAddress = getServerIpAddress(ipString);
                Thread connect = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Socket socket = null;
                        PrintWriter out = null;
                        BufferedReader in = null;
                        try {
                            socket = new Socket(serverIpAddress, Utilities.ANDROID_PORT);
                            out =  new PrintWriter(socket.getOutputStream(), true);
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            out.print("ANDROID");
                            String response;
                            while (((response = in.readLine()) != null)) {
                                System.out.println("Response: " + response);
                                if (response.contains("OK")) {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("serverIpAddress", serverIpAddress);
                                    startActivity(intent);
                                    finish();
                                }
                                if (response.contains("NO")){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtMessage.setText(getResources().getString(R.string.over_capacity));
                                            imgMessage.setImageResource(R.drawable.icons8_offline);
                                        }
                                    });
                                }
                            }
                        } catch (IOException ex) {
                            Log.e(this.getClass().getName(), ex.getMessage());
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
                connect.start();
            }
        }
    }

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
            Log.e("WIFI IP", "Unable to get host address.");
            ipString = null;
        }
        return ipString;
    }

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

    private void initComponent() {
        txtMessage = (TextView) findViewById(R.id.txtMessage);
        imgMessage = (ImageView) findViewById(R.id.imgMessage);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        imgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();
            }
        });
    }

}