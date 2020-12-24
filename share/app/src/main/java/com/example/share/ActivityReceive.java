package com.example.share;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ActivityReceive extends AppCompatActivity
{
    private static String TAG = ActivityReceive.class.toString();
    private Button btnWifi;
    private Button btnLocation;
    private Button btnHotSpot;
    private LocationManager locationManager;
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        setContentView(R.layout.activity_send);
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        checkState();
        initialize();
    }

    private void initialize()
    {
        btnWifi = findViewById(R.id.btnWifi);
        btnLocation = findViewById(R.id.btnLocation);
        btnHotSpot = findViewById(R.id.btnHotSpot);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        BroadCastReceiver broadCast = new BroadCastReceiver(this);
        registerReceiver(broadCast,intentFilter);

        btnWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wifiManager.isWifiEnabled()){
                    if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                        wifiManager.setWifiEnabled(true);
                    }else{
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    ActivityReceive.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            }
        });


        refreshWifi();
        refreshLocation();

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshWifi();
        refreshLocation();
    }

    public void refreshWifi(){
        if(wifiManager.isWifiEnabled())
        {
            btnWifi.setClickable(false);
            Drawable img = this.getResources().getDrawable(R.drawable.ic_wifi,null);
            img.setBounds(0,0,50,50);
            btnWifi.setCompoundDrawables(img,null,null,null);
            btnWifi.setTextColor(getApplicationContext().getResources().getColor(R.color.colorLightBlack));
            checkState();
        }else{
            btnWifi.setClickable(true);
            Drawable img = this.getResources().getDrawable(R.drawable.ic_signal_off,null);
            img.setBounds(0,0,50,50);
            btnWifi.setCompoundDrawables(img,null,null,null);
            btnWifi.setTextColor(getApplicationContext().getResources().getColor(R.color.colorBlack));
        }
    }

    public void refreshLocation()
    {
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Drawable img = this.getResources().getDrawable(R.drawable.ic_location_on,null);
            img.setBounds(0,0,50,50);
            btnLocation.setCompoundDrawables(img,null,null,null);
            btnLocation.setTextColor(getApplicationContext().getResources().getColor(R.color.colorLightBlack));
            btnLocation.setClickable(false);
            checkState();
        }else{
            Drawable img = this.getResources().getDrawable(R.drawable.ic_location_off,null);
            img.setBounds(0,0,50,50);
            btnLocation.setCompoundDrawables(img,null,null,null);
            btnLocation.setTextColor(getApplicationContext().getResources().getColor(R.color.colorBlack));
            btnLocation.setClickable(true);
        }
    }

    public void resfreshHotSpot(boolean hotspotEnabled) {
        if(hotspotEnabled) {
            Drawable img = getResources().getDrawable(R.drawable.ic_hotspot_on,null);
            img.setBounds(0,0,50,50);
            btnHotSpot.setCompoundDrawables(img,null,null,null);
            btnHotSpot.setTextColor(getApplicationContext().getResources().getColor(R.color.colorBlack));
            btnHotSpot.setClickable(true);
        }else{
            Drawable img = getResources().getDrawable(R.drawable.ic_hotspot_off,null);
            img.setBounds(0,0,50,50);
            btnHotSpot.setCompoundDrawables(img,null,null,null);
            btnHotSpot.setTextColor(getApplicationContext().getResources().getColor(R.color.colorLightBlack));
            btnHotSpot.setClickable(false);
            checkState();
        }
    }

    public void checkState()
    {
        if(wifiManager.isWifiEnabled()&&locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Log.e(TAG,"------");
            startActivity(new Intent(ActivityReceive.this, ActivitySearchWifi_Receive.class));
        }
    }

}