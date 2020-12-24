package com.example.share;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class BroadCastReceiver extends BroadcastReceiver
{
    private ActivitySend activitySend;
    private ActivityReceive activityReceive;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager wifiP2pManager;
    private ActivitySearchWifi_Receive activitySearchWifi_receive;
    private ActivitySearchWifi_Send activitySearchWifi_send;

    public BroadCastReceiver()
    {

    }

    public BroadCastReceiver(ActivityReceive activityReceive){
        this.activityReceive = activityReceive;
    }

    public BroadCastReceiver(ActivitySend activitySend){
        this.activitySend = activitySend;
    }

    public BroadCastReceiver(ActivitySearchWifi_Receive activitySearchWifi_receive,WifiP2pManager.Channel mChannel,WifiP2pManager wifiP2pManager){
        this.activitySearchWifi_receive = activitySearchWifi_receive;
        this.mChannel = mChannel;
        this.wifiP2pManager = wifiP2pManager;
    }

    public BroadCastReceiver(ActivitySearchWifi_Send activitySearchWifi_send,WifiP2pManager.Channel mChannel,WifiP2pManager wifiP2pManager){
        this.activitySearchWifi_send = activitySearchWifi_send;
        this.mChannel = mChannel;
        this.wifiP2pManager = wifiP2pManager;
    }


    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")){
            int state = intent.getIntExtra("wifi_state",0);
            if(state == 13){
                if(activitySend!=null){
                    activitySend.resfreshHotSpot(true);
                }
                if(activityReceive!=null){
                    activityReceive.resfreshHotSpot(true);
                }
            }else if(state==11){
                if(activitySend!=null){
                    activitySend.resfreshHotSpot(false);
                }
                if(activityReceive!=null){
                    activityReceive.resfreshHotSpot(false);
                }
            }
        }else if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){

            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,-1);

            if(state==WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                if(activitySend!=null){
                    activitySend.refreshWifi();
                }

                if(activityReceive!=null){
                    activitySend.refreshWifi();;
                }

            }else{
                if(activitySend!=null){
                    activitySend.refreshWifi();
                }

                if(activityReceive!=null){
                    activityReceive.refreshWifi();;
                }
            }

        }else if(action.equals(LocationManager.PROVIDERS_CHANGED_ACTION))
        {
            if(activitySend!=null){
                activitySend.refreshLocation();
            }
            if(activityReceive!=null){
                activityReceive.refreshLocation();;
            }

        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            if(wifiP2pManager==null)
            {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkInfo.isConnected())
            {
                Log.e("--------",networkInfo.toString());
                if(activitySearchWifi_receive!=null){
                    wifiP2pManager.requestConnectionInfo(mChannel,activitySearchWifi_receive.connectionInfoListener);
                }else if(activitySearchWifi_send!=null){
                    wifiP2pManager.requestConnectionInfo(mChannel,activitySearchWifi_send.connectionInfoListener);
                }
            }

        }else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            if(wifiP2pManager!=null){

                if(activitySearchWifi_receive!=null){
                    if (ActivityCompat.checkSelfPermission(activitySearchWifi_receive, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    wifiP2pManager.requestPeers(mChannel, activitySearchWifi_receive.peerListListener);
                }else if(activitySearchWifi_send!=null){
                    if (ActivityCompat.checkSelfPermission(activitySearchWifi_send, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    wifiP2pManager.requestPeers(mChannel, activitySearchWifi_send.peerListListener);
                }
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            String macAddress = device.deviceAddress;
            Log.e("Mac address broadcast",macAddress);
            activitySearchWifi_receive.setMacAddress(macAddress);
        }
    }

}
