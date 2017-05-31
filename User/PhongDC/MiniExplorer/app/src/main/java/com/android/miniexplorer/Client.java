package com.android.miniexplorer;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by Phong on 6/1/2017.
 */

public class Client extends AsyncTask<Void, Void, Void>{

    String svAddress;
    int svPort;
    JSONObject data;

    Client(String address, int port, JSONObject json) {
        svAddress = address;
        svPort = port;
        data = json;
    }

    @Override
    protected  Void doInBackground(Void... arg0) {
        Socket socket = null;

        try {
            socket = new Socket(svAddress, svPort);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(data.toString());
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e("ERROR", e.getMessage());
                }
            }
        }
        return null;
    }
}
