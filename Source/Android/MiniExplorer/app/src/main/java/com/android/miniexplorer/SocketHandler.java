package com.android.miniexplorer;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class SocketHandler {

    private static Socket socket;

    public static synchronized Socket getSocket() {
        return socket;
    }

    public static synchronized void setSocket(Socket socket) {
        SocketHandler.socket = socket;
    }

    public static synchronized void closeSocket() {
        if (SocketHandler.socket != null) {
            try {
                SocketHandler.socket.close();
            } catch (IOException ex) {
                Log.e(SocketHandler.class.getName(), ex.toString());
            }
        }
    }
}
