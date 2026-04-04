package com.XiTu893.TokenEdge.service;

public class ServiceManager {
    private static ServiceManager instance;
    private boolean isServiceRunning = false;
    private int currentPort = -1;
    private static final int START_PORT = 3000;
    private static final int MAX_PORT = 3999;

    private ServiceManager() {}

    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    public void setServiceRunning(boolean running) {
        this.isServiceRunning = running;
        if (!running) {
            currentPort = -1;
        }
    }

    public int getCurrentPort() {
        return currentPort;
    }

    public void setCurrentPort(int port) {
        this.currentPort = port;
    }

    public int findAvailablePort() {
        for (int port = START_PORT; port <= MAX_PORT; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        return -1;
    }

    private boolean isPortAvailable(int port) {
        try {
            java.net.ServerSocket socket = new java.net.ServerSocket(port);
            socket.close();
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }
}
