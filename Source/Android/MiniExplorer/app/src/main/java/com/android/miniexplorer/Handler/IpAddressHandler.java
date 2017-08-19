package com.android.miniexplorer.Handler;

public class IpAddressHandler {

    private static String deviceIpAddress;
    private static String serverIpAddress;

    public static String getDeviceIpAddress() {
        return deviceIpAddress;
    }

    public static void setDeviceIpAddress(String deviceIpAddress) {
        IpAddressHandler.deviceIpAddress = deviceIpAddress;
    }

    public static String getServerIpAddress() {
        return serverIpAddress;
    }

    public static void setServerIpAddress(String serverIpAddress) {
        IpAddressHandler.serverIpAddress = serverIpAddress;
    }
}
