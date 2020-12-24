package com.example.share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class ActivityChooser extends AppCompatActivity
{
    private LinearLayout history;
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,};
    private static final int REQUEST_PERMISSION = 1254;
    private static final int PERMISSIONCOUNT = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(arePermissionDenied())
            {
                requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            }
        }


        findViewById(R.id.liSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(ActivityChooser.this,MainActivity.class));
            }
        });

        findViewById(R.id.liReceive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                WifiManager wifiManager =(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager.isWifiEnabled() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    startActivity(new Intent(ActivityChooser.this, ActivitySearchWifi_Receive.class));
                }else{
                    startActivity(new Intent(ActivityChooser.this,ActivityReceive.class));
                }
            }
        });

        history= findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityChooser.this,History.class));
            }
        });
    }

    //Permission Result as per the user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION&&grantResults.length>0)
        {

        }else{
//            startActivity(new Intent(ActivitySearchWifi_Receive.this,ActivityChooser.class));
        }
    }

    //ask for permission if the permission is denied
    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(arePermissionDenied()){
                requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            }
        }
    }

    private boolean arePermissionDenied()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            int p = 0;

            while (p<PERMISSIONCOUNT)
            {
                if(checkSelfPermission(PERMISSIONS[p])== PackageManager.PERMISSION_DENIED)
                {
                    return true;
                }
                p++;
            }
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }
}