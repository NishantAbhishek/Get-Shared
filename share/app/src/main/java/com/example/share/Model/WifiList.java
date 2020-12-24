package com.example.share.Model;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiList {
    private WifiP2pDevice wifiP2pDevice;
    private int adapterType;

    public WifiList(WifiP2pDevice wifiP2pDevice, int adapterType) {
        this.wifiP2pDevice = wifiP2pDevice;
        this.adapterType = adapterType;
    }

    public WifiP2pDevice getWifiP2pDevice() {
        return wifiP2pDevice;
    }

    public void setWifiP2pDevice(WifiP2pDevice wifiP2pDevice) {
        this.wifiP2pDevice = wifiP2pDevice;
    }

    public int getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(int adapterType) {
        this.adapterType = adapterType;
    }
}
