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

import java.io.DataOutputStream;
import java.io.IOException;
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

    @Override
    protected void onResume() {
        super.onResume();
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
                        try {
                            socket = new Socket(serverIpAddress, Utilities.ANDROID_PORT);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("serverIpAddress", serverIpAddress);
                            startActivity(intent);
                            finish();
                        } catch (IOException ex) {
                            Log.e(this.getClass().getName(), ex.getMessage());
                        } finally {
                            if (socket != null) {
                                try {
                                    socket.close();
                                } catch (IOException ex) {
                                    System.out.println(ex.getMessage());
                                }
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
