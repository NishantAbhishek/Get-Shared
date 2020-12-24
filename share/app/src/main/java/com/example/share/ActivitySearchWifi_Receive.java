package com.example.share;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.share.Adapter.WifiListAdapter;
import com.example.share.Helper.ConnectionSuccess;
import com.example.share.Helper.Constants;
import com.example.share.Model.WifiList;
import com.google.zxing.WriterException;

import java.net.InetAddress;

import java.util.ArrayList;


public class ActivitySearchWifi_Receive extends AppCompatActivity implements ServiceReceive.OnProgressUpdateListener, ConnectionSuccess
{
    private static final String TAG = ActivitySearchWifi_Receive.class.toString();
    private ImageView imageAnim1, imageAnim2;
    private ConstraintLayout Cons_show_progress;
    private ImageView imageBarcode;
    private Handler handler;
    private TextView phoneName;
    private WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel mChannel;
    private String macAddress;
    private RecyclerView recyclerView;
    private WifiListAdapter wifiListAdapter;
    ArrayList<WifiList> peers = new ArrayList<>();
    private LinearLayout li_barCode;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 111, REQUEST_WRITE_STORAGE = 112;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_wifi__receive);
        CHECK_PERMISSION();
        instantiateViews();
        startAnimatimation();
        instantiateRecycler();
        createBarcode();
        instantiatBroadCast();
        discoverPeers();
    }


    private void CHECK_PERMISSION()
    {
        boolean hasPermissionLocation = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermissionLocation) {
            ActivityCompat.requestPermissions(ActivitySearchWifi_Receive.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }
        boolean hasPermissionWrite = (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermissionWrite) {
            ActivityCompat.requestPermissions(ActivitySearchWifi_Receive.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    finish();
                    startActivity(getIntent());
                }
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(ActivitySearchWifi_Receive.this, "Permission granted.", Toast.LENGTH_SHORT).show();
                    //reload my activity with permission granted or use the features what required the permission
                    finish();
                    startActivity(getIntent());
                }
            }
    }
    }

    //creates the barcode checks android version first
    private void createBarcode(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

        }else{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q)
            {
                wifiP2pManager.requestDeviceInfo(mChannel, new WifiP2pManager.DeviceInfoListener()
                {
                    @Override
                    public void onDeviceInfoAvailable(@Nullable WifiP2pDevice wifiP2pDevice)
                    {
                        macAddress = wifiP2pDevice.deviceAddress;
                        if(macAddress!=null){
                            createBarcode(macAddress);
                        }
                    }
                });
            }
        }

    }

    //instantitate normal layout views in android
    private void instantiateViews()
    {
        imageAnim1 = findViewById(R.id.imagAnim1);
        imageAnim2 = findViewById(R.id.imagAnim2);
        imageBarcode = findViewById(R.id.img_Barcode);
        li_barCode = findViewById(R.id.li_barCode);
        phoneName = findViewById(R.id.deviceName);
        phoneName.setText(Build.MODEL);
        wifiP2pManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = wifiP2pManager.initialize(this, getMainLooper(), null);
        Cons_show_progress = findViewById(R.id.Cons_show_progress);
        Cons_show_progress.setVisibility(View.GONE);

        if(Build.VERSION.SDK_INT==Build.VERSION_CODES.Q){
            li_barCode.setVisibility(View.GONE);
        }

    }

    //instatintaite recycler which holds wifiP2P data
    private void instantiateRecycler(){
        recyclerView = findViewById(R.id.wifiList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        wifiListAdapter = new WifiListAdapter(peers,this,mChannel,wifiP2pManager,this);
        recyclerView.setAdapter(wifiListAdapter);
    }

    //instatatiate and resister Broadcast which checks wifi state and connection and device mac info and peers changed
    private void instantiatBroadCast(){
        BroadCastReceiver receiver = new BroadCastReceiver(this,mChannel,wifiP2pManager);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(receiver,intentFilter);

    }

    //discovers peers when the activity starts
    private void discoverPeers()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        wifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                Toast.makeText(ActivitySearchWifi_Receive.this,"Discovering Signals......",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(int reason) {

            }
        });
    }

    //update the list of peers after listenting from broadcast
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener()
    {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList)
        {
            if(!peers.equals(peerList)){
                peers.clear();
                wifiListAdapter.notifyDataSetChanged();

                for(WifiP2pDevice device:peerList.getDeviceList()){
                    peers.add(new WifiList(device, Constants.ADAPTER_RECEIVE));
                    wifiListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    //create barcode which holds the mac adddress of the receive devuce and shows it as QRcode which will be scanned
    private void createBarcode(String macAddress)
    {
        QRGEncoder qrgEncoder = new QRGEncoder(macAddress+Build.MODEL,null, QRGContents.Type.TEXT,500);
        try{
            Bitmap qrBits = qrgEncoder.encodeAsBitmap();
            imageBarcode.setImageBitmap(qrBits);
        }catch (WriterException e){
            e.printStackTrace();
        }
    }


    //animation starts-------------------
    private void startAnimatimation(){
        handler = new Handler(getMainLooper());

        this.runnable.run();
    }

    private Runnable runnable = new Runnable()
    {
        @Override
        public void run()
        {
            imageAnim1.animate().scaleX(4f).setDuration(1100).scaleY(4f).alpha(0f).withEndAction(new Runnable() {
                @Override
                public void run()
                {
                    imageAnim1.setScaleX(0f);
                    imageAnim1.setScaleY(0f);
                    imageAnim1.setAlpha(1f);
                }
            });
            imageAnim2.animate().scaleX(4f).setDuration(500).scaleY(4f).alpha(0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    imageAnim2.setScaleX(0f);
                    imageAnim2.setScaleY(0f);
                    imageAnim2.setAlpha(1f);
                }
            });

            handler.postDelayed(runnable,1500);
        }
    };
    //animation ends-------------------


    //creates barcode after receiveing Broadcast of macAdress WIFI_P2P_THIS_DEVICE_CHANGED_ACTION which changes devices macAddress
    public void setMacAddress(String macAddress)
    {
        this.macAddress = macAddress;

        if(macAddress!=null)
        {
            createBarcode(macAddress);
        }
    }


    //called whenever there is connection estableshed and host and client is formed
    public String owner;
    public String IpAddress;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info)
        {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;//it returns the address of group owner address
            Log.e(TAG+"---Ip---"+groupOwnerAddress.getHostAddress(),"Ownner"+Boolean.toString(info.isGroupOwner));
            owner = Boolean.toString(info.isGroupOwner);
            IpAddress = groupOwnerAddress.getHostAddress();
            Log.e(TAG+"---Ip---"+groupOwnerAddress.getHostAddress(),"Ownner"+Boolean.toString(info.isGroupOwner));
            connectionSuccess();
            startService();
        }
    };

    public void connectionSuccess(){
        recyclerView.setVisibility(View.GONE);
        li_barCode.setVisibility(View.GONE);
        Cons_show_progress.setVisibility(View.VISIBLE);
    }

    //Service Code starts here------------
    private ServiceReceive serviceReceive;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;
    private ProgressBar fullProgress, fileProgress;
    private TextView tvFileName, tvPercent;


    private void startService(){
        instatatiateViews();
        serviceIntent = new Intent(ActivitySearchWifi_Receive.this,ServiceReceive.class);

        try {
            serviceIntent.putExtra("ipAddress",IpAddress);
            serviceIntent.putExtra("owner",owner);
        }catch (Exception e){
            e.printStackTrace();
        }
        startService(serviceIntent);
        bindService();
    }

    public void instatatiateViews() {
        fullProgress = findViewById(R.id.fullProgress);
        fileProgress = findViewById(R.id.fileProgress);
        tvFileName = findViewById(R.id.fileName);
        tvPercent = findViewById(R.id.tvPercent);
    }

    private void bindService()
    {
        if(serviceConnection==null){
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ServiceReceive.MyServiceBinder binder = (ServiceReceive.MyServiceBinder) service;
                    serviceReceive = binder.getService();
                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            bindService(serviceIntent,serviceConnection,Context.BIND_AUTO_CREATE);
            serviceReceive.setOnProgressChangedListener(this);
        }
    }

    private void unBindService() {
        unbindService(serviceConnection);
    }


    @Override
    public void setPercent(final int Percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvPercent.setText(Integer.toString(Percent) + " %");
            }
        });

    }

    @Override
    public void setFullMax(final int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fullProgress.setMax(100);
            }
        });
    }

    @Override
    public void setfileMax(final int max) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fileProgress.setMax(100);
            }
        });
    }

    @Override
    public void increFileProgress(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fileProgress.setProgress(progress, true);
                } else {
                    fullProgress.setProgress(progress);
                }

            }
        });
    }

    @Override
    public void increFullProgress(final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fullProgress.setProgress(progress, true);
                } else {
                    fullProgress.setProgress(progress);
                }
            }
        });
    }

    @Override
    public void refreshEverthing() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fullProgress.setProgress(0, true);
                } else {
                    fullProgress.setProgress(0);
                }
            }
        });
    }

    @Override
    public void setFileName(final String fileName) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvFileName.setText(fileName);
            }
        });
    }

    @Override
    public void transferComplete(boolean complete) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnectWifiP2p();
                tvFileName.setText("Transfer Complete");
                unBindService();
                stopService(serviceIntent);
            }
        });
    }

    //disconnect from wifiP2p Connection

    public void disconnectWifiP2p() {
        if (wifiP2pManager != null && mChannel != null)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            wifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group)
                {
                    if(group!=null && wifiP2pManager!=null && mChannel!=null){
                        wifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess()
                            {
                                startActivity(new Intent(getApplicationContext(),History.class));
                                //do something after the connection is closed
                            }
                            @Override
                            public void onFailure(int reason)
                            {
                                Toast.makeText(getApplicationContext(),"Connection is still there",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }
    }

    //pressing back buttons sends the user to Activity chooser
    @Override
    public void onBackPressed(){
        if(serviceReceive!=null){
            if(serviceReceive.isServiceRunning()){
                Toast.makeText(getApplicationContext(),"Wait... Transferring Data",Toast.LENGTH_LONG).show();
            }
        }else{
            startActivity(new Intent(ActivitySearchWifi_Receive.this,ActivityChooser.class));
        }
    }

    @Override
    public void onConnectionSuccess() {

    }
}