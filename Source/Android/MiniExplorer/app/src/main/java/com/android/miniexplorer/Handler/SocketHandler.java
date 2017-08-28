package com.android.miniexplorer.Handler;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class SocketHandler {

    final static String LOG_TAG = "Socket Handler";

    private static Socket socket;

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }

    public static void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
    }
}
