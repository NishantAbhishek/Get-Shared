package com.example.share;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
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
import com.example.share.Model.FileItems;
import com.example.share.Model.WifiList;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

public class ActivitySearchWifi_Send extends AppCompatActivity implements ServiceSend.OnProgressUpdateListener, ConnectionSuccess
{
    private static final String TAG = ActivitySearchWifi_Send.class.toString();
    private ConstraintLayout Cons_show_progress;
    private ImageView imageAnim1, imageAnim2;
    private Handler handler;
    private TextView phoneName;
    private LinearLayout li_Scan;
    private ArrayList<FileItems> stringSelected;

    private WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel mChannel;
    private RecyclerView recyclerView;
    private WifiListAdapter wifiListAdapter;
    ArrayList<WifiList> peers = new ArrayList<>();

    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION = 1234;
    private static final int PERMISSIONCOUNT = 4;

    //host will send the data
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_wifi);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(arePermissionDenied())
            {
                requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            }
        }

        wifiP2pManager = (WifiP2pManager) getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = wifiP2pManager.initialize(this, getMainLooper(), null);
        Cons_show_progress = findViewById(R.id.Cons_show_progress);
        Cons_show_progress.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        stringSelected = (ArrayList<FileItems>) bundle.getSerializable("selectedData");
        Log.e(TAG, "size:- " + Integer.toString(stringSelected.size()));

        checkCameFromBarcode();

        imageAnim1 = findViewById(R.id.imagAnim1);
        imageAnim2 = findViewById(R.id.imagAnim2);
        li_Scan = findViewById(R.id.li_Scan);
        startAnimatimation();

        phoneName = findViewById(R.id.deviceName);
        phoneName.setText(Build.MODEL);


        scanClick();
        instantiateRecycler();
        instantiatBroadCast();
        discoverPeers();
    }

    //checking for permission
    private boolean arePermissionDenied()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int p = 0;

            while (p<PERMISSIONCOUNT){
                if(checkSelfPermission(PERMISSIONS[p])==PackageManager.PERMISSION_GRANTED){
                    return true;
                }
                p++;
            }
        }
        return false;
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


    //Permission Result as per the user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION&&grantResults.length>0)
        {
            discoverPeers();
        }else{
//            startActivity(new Intent(ActivitySearchWifi_Receive.this,ActivityChooser.class));
        }
    }


    //this method starts the barcode scanner and send the filePath which were intitialyy selected
    private void scanClick() {
        li_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedData", stringSelected);
                Intent intent = new Intent(ActivitySearchWifi_Send.this, BarcodeActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    //this method instantiate Broadcast which gives Broad cast about connection change, peers Change and WifiState Changed
    private void instantiatBroadCast() {
        BroadCastReceiver receiver = new BroadCastReceiver(this, mChannel, wifiP2pManager);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    //this method instantiate the Recycler containg all the wifiP2p
    private void instantiateRecycler() {
        recyclerView = findViewById(R.id.wifiList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        wifiListAdapter = new WifiListAdapter(peers, this, mChannel, wifiP2pManager,this);
        recyclerView.setAdapter(wifiListAdapter);
    }


    //check wether barcode gave back the string selected which comes back after scanning of the barcode
    private void checkCameFromBarcode() {
        String macAddress = getIntent().getStringExtra("macAddress");
        if (macAddress != null) {

            Log.e(macAddress + " :-", Integer.toString(stringSelected.size()));
            connect(macAddress);
        }
    }

    //Here I try to connect through the barcode recieved from the scanner
    private void connect(String macAddress) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = macAddress;
        Log.e(macAddress, macAddress);

        if (ActivityCompat.checkSelfPermission(ActivitySearchWifi_Send.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.e(macAddress, macAddress);

        wifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(),"Connected Successfully ",Toast.LENGTH_LONG).show();
                onConnectionSuccess();
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    //In the beginning this method is called in order to discover the peers
    private void discoverPeers() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        wifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess() {
                Toast.makeText(ActivitySearchWifi_Send.this, "Discovering Signals......", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }


    //called from the broadcast whenever there is change in wifi peers
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peers.equals(peerList)) {
                peers.clear();
                wifiListAdapter.notifyDataSetChanged();

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    peers.add(new WifiList(device, Constants.ADAPTER_SEND));
                    wifiListAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    //starts the circular animation
    private void startAnimatimation() {
        handler = new Handler(getMainLooper());
        this.runnable.run();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            imageAnim1.animate().scaleX(4f).setDuration(1100).scaleY(4f).alpha(0f).withEndAction(new Runnable() {
                @Override
                public void run() {
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
            handler.postDelayed(runnable, 1500);
        }
    };





    public String owner;
    public String IpAddress;
    //Called from the broadcast after the connection is established
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener(){
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            InetAddress groupOwnerAddress = info.groupOwnerAddress;
            Log.e(TAG + "---Ip---", "Owner" + Boolean.toString(info.isGroupOwner));
            IpAddress = groupOwnerAddress.getHostAddress();
            owner = Boolean.toString(info.isGroupOwner);
            startService();
        }
    };

    public void onConnectionSuccess(){
        recyclerView.setVisibility(View.GONE);
        li_Scan.setVisibility(View.GONE);
        Cons_show_progress.setVisibility(View.VISIBLE);
    }

    //Service Implemetation-------------------
    //start and bind the service inorder to transfer the data

    private ServiceSend serviceSend;
    private Intent serviceIntent;
    private ServiceConnection serviceConnection;
    private ProgressBar fullProgress, fileProgress;
    private TextView tvFileName, tvPercent;


    private void startService() {
        instatatiateViews();
        if (stringSelected.size() == 1) {
            if(!new File(stringSelected.get(0).getPath()).isDirectory()){
                fileProgress.setVisibility(View.INVISIBLE);
            }
        }
        //service created
        serviceIntent = new Intent(ActivitySearchWifi_Send.this, ServiceSend.class);
        try {
            serviceIntent.putExtra("owner", owner);
            serviceIntent.putExtra("ipAddress", IpAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedData", stringSelected);
        serviceIntent.putExtras(bundle);
        startService(serviceIntent);
        //service bound
        bindService();
    }


    public void instatatiateViews() {
        fullProgress = findViewById(R.id.fullProgress);
        fileProgress = findViewById(R.id.fileProgress);
        tvFileName = findViewById(R.id.fileName);
        tvPercent = findViewById(R.id.tvPercent);
    }


    private void bindService() {
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    ServiceSend.MySeriviceBinder binder = (ServiceSend.MySeriviceBinder) service;
                    serviceSend = binder.getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        serviceSend.setOnProgressChangedListener(this);
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
        if (wifiP2pManager != null && mChannel != null) {
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

    //called whwen the user presses the back button
    @Override
    public void onBackPressed(){
        if(serviceSend!=null){
            if(serviceSend.isServiceRunning()){
                Toast.makeText(getApplicationContext(),"Wait... Transferring Data",Toast.LENGTH_LONG).show();
            }
        }else{
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            Bundle bundle = new Bundle();
            bundle.putSerializable("selectedData", stringSelected);

            if (wifiManager.isWifiEnabled() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Intent intent = new Intent(ActivitySearchWifi_Send.this, MainActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            } else {
                Intent intent = new Intent(ActivitySearchWifi_Send.this, ActivitySend.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }



}