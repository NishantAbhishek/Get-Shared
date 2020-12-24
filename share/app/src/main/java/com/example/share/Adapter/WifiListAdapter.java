package com.example.share.Adapter;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.share.Helper.ConnectionSuccess;
import com.example.share.Helper.Constants;
import com.example.share.Model.WifiList;
import com.example.share.R;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.WifiViewHolder> {
    private static final String TAG = WifiListAdapter.class.toString();
    private ArrayList<WifiList> wifiList;
    private Context mContext;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager wifiP2pManager;
    private ConnectionSuccess connectionSuccess;


    public WifiListAdapter(ArrayList<WifiList> wifiList, Context mContext, WifiP2pManager.Channel mChannel, WifiP2pManager wifiP2pManager, ConnectionSuccess connectionSuccess){
        this.wifiList = wifiList;
        this.mContext = mContext;
        this.mChannel = mChannel;
        this.wifiP2pManager = wifiP2pManager;
        this.connectionSuccess = connectionSuccess;

    }

    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.wifi_item, parent, false);
        return new WifiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WifiViewHolder holder, int position){
        holder.deviceName.setText(wifiList.get(position).getWifiP2pDevice().deviceName);
        if(wifiList.get(position).getAdapterType() == Constants.ADAPTER_RECEIVE){
            holder.btnConnect.setVisibility(View.GONE);
        }else{
            holder.btnClick(wifiList.get(position).getWifiP2pDevice().deviceAddress);
            holder.btnConnect.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    class WifiViewHolder extends RecyclerView.ViewHolder
    {

        public Button btnConnect;
        public TextView deviceName;

        public WifiViewHolder(@NonNull View itemView)
        {
            super(itemView);
            btnConnect = itemView.findViewById(R.id.btnConnect);
            deviceName = itemView.findViewById(R.id.tvName);
        }

        public void btnClick(String deviceAddress){
            final WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = deviceAddress;
            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"Connecting....",Toast.LENGTH_LONG).show();
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        Toast.makeText(mContext,"Please Provider Location Permission",Toast.LENGTH_LONG).show();
                    }
                    wifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener(){
                        @Override
                        public void onSuccess()
                        {
                            Toast.makeText(mContext,"Connected Successfully ",Toast.LENGTH_LONG).show();
                            connectionSuccess.onConnectionSuccess();
                        }
                        @Override
                        public void onFailure(int reason)
                        {
                            Toast.makeText(mContext,"Connection Problem",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }
}