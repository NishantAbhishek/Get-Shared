package com.example.share;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.share.Adapter.FileAdapter;
import com.example.share.Adapter.MainSelectedAdapter;
import com.example.share.Adapter.PathAdapter;
import com.example.share.Helper.Constants;
import com.example.share.Helper.HeaderClick;
import com.example.share.Helper.LoadData;
import com.example.share.Model.FileItems;
import com.example.share.Model.SelectedItems;
import com.example.share.Model.StringSelected;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity
{
    public static String TAG = MainActivity.class.toString();
    private static final String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION = 1234;
    private static final int PERMISSIONCOUNT =2;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private PathAdapter pathAdapter;
    private ArrayList<FileItems> fileItems = new ArrayList<>();
    private static LinearLayout pathNavigation;
    public static LinearLayout liHomeDirect;
    private static ArrayList<FileItems> selectedFile;
    private static LinearLayout btnSend;
    private static TextView tvNumberItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checking the permission
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(arePermissionDenied())
            {
                requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            }
        }

        Instantiate();
    }

    private void Instantiate()
    {
        selectedFile = new ArrayList<>();

        //instantiate the pathAdapter
        pathAdapter = new PathAdapter(this);
        pathAdapter.setAdapter(pathAdapter);
        LinearLayoutManager pathManager = new LinearLayoutManager(getApplicationContext());
        pathManager.setOrientation(RecyclerView.HORIZONTAL);
        RecyclerView pathRecycler = findViewById(R.id.pathRecycle);
        pathRecycler.setLayoutManager(pathManager);
        pathRecycler.setAdapter(pathAdapter);

        //instatitiate the recycler view for list of files
        recyclerView = (RecyclerView) findViewById(R.id.list_files);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        fileAdapter = new FileAdapter(getApplicationContext(),fileItems);
        fileAdapter.setFileAdapter(pathAdapter);
        recyclerView.setAdapter(fileAdapter);

        //loadData thread to retrieve all the data in background thread
        final LoadData loadData = new LoadData(fileAdapter,MainActivity.this,fileItems,recyclerView);
        loadData.start();
        fileAdapter.setLoadMore(loadData);
        pathAdapter.setLoadMore(loadData);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //mainActivity Header to load all the images
        LinearLayout[] headerLayouts = {findViewById(R.id.liFile),findViewById(R.id.liApk), findViewById(R.id.liPdfs),findViewById(R.id.liImage),findViewById(R.id.liAudio),findViewById(R.id.liVideo)};
        pathNavigation = findViewById(R.id.linear_item);
        liHomeDirect = findViewById(R.id.liHomeDirect);

        //headerClick to load different kind of data
        HeaderClick headerClick = new HeaderClick(this,this,loadData,fileAdapter,recyclerView);

        for (int i = 0; i < headerLayouts.length; i++)
        {
            headerLayouts[i].setOnClickListener(headerClick);
        }

        liHomeDirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Message message = new Message();
                message.obj = Constants.HOMEDIRECTORY;
                loadData.handler.sendMessage(message);
                pathAdapter.resetInterface();
            }
        });

        btnSend = findViewById(R.id.btnSend);
        tvNumberItem = findViewById(R.id.itemNumber);
        ConstraintLayout btnConstraint = findViewById(R.id.ConsBag);

        btnConstraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSelectedDialog();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ArrayList<FileItems>  selected = new ArrayList<>();
                btnSend.setBackground(getResources().getDrawable(R.drawable.btn_send_background_white,null));

                selected.addAll(selectedFile);

                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedData",selected);

                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
                WifiManager wifiManager =(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager.isWifiEnabled() && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    startActivity(new Intent(MainActivity.this, ActivitySearchWifi_Send.class).putExtras(bundle));
                }else{
                    startActivity(new Intent(MainActivity.this,ActivitySend.class).putExtras(bundle));
                }
            }
        });


        Bundle bundle = getIntent().getExtras();

        if(bundle!=null)
        {
            ArrayList<FileItems> stringSelected =(ArrayList<FileItems>) bundle.getSerializable("selectedData");
            selectedFile.addAll(stringSelected);

            fileAdapter.notifyDataSetChanged();
            refreshBtnStatus();
            headerClick.setAllFalse();
        }
    }

    //reset MainActivity and toggle tge disappareace of pathNavigation
    public static void resetMainActivity(boolean VISIBILITY)
    {
        if(VISIBILITY==false){
            pathNavigation.setVisibility(View.GONE);
        }else {
            pathNavigation.setVisibility(View.VISIBLE);
        }
    }

    //checking permission -------------------------------------------
    private boolean arePermissionDenied()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            int p = 0;

            while (p<PERMISSIONCOUNT)
            {
                if(checkSelfPermission(PERMISSIONS[p])== PackageManager.PERMISSION_DENIED)
                {
                    Log.e(TAG,"Asking for permission");

                    return true;
                }
                p++;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(arePermissionDenied()){
                requestPermissions(PERMISSIONS,REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSION && grantResults.length>0){
            if(arePermissionDenied())
            {

            }else{
                Instantiate();
            }
        }
    }

    public static void addSelection(FileItems fileItems)
    {
        selectedFile.add(fileItems);
        refreshBtnStatus();
    }

    public static boolean isSelected(FileItems fileItems)
    {
        for(FileItems data:selectedFile)
        {
            if(data.getPath().equals(fileItems.getPath()))
            {
                return true;
            }
        }
        return false;
    }


    //remove the selected item after clicking the check box
    public static void removeSelected(String filePath){
        ListIterator ltr = selectedFile.listIterator();

        while (ltr.hasNext()){
            FileItems fileItems = (FileItems) ltr.next();
            String path = fileItems.getPath();
            if(path.equals(filePath)){
                ltr.remove();
            }
            Log.e(TAG+"-->",path);
        }
        refreshBtnStatus();
    }

    //checks the size of the sendBtn and toggle visiblity
    public static void refreshBtnStatus()
    {
        if(selectedFile.size()>0){
            btnSend.setVisibility(View.VISIBLE);
            tvNumberItem.setText(Integer.toString(selectedFile.size()));

        }else{
            btnSend.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(!PathAdapter.isPathEmpty())
        {
            pathAdapter.popTop();
        }else{
            startActivity(new Intent(MainActivity.this,ActivityChooser.class));
        }
    }

    //recycler view insdide a dilog
    private Dialog dialog;
    public void createSelectedDialog()
    {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog_item_selected);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialog_open));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        dialog.show();

        ArrayList<FileItems> Videos = new ArrayList<>();
        ArrayList<FileItems> Application = new ArrayList<>();
        ArrayList<FileItems> Images = new ArrayList<>();
        ArrayList<FileItems> Directory = new ArrayList<>();
        ArrayList<FileItems> Document = new ArrayList<>();
        ArrayList<FileItems> Audio = new ArrayList<>();
        ArrayList<FileItems> Others = new ArrayList<>();


        for(FileItems items:selectedFile)
        {
            if(items.getFileType()==Constants.VIDEO){
                Videos.add(items);
            }else if(items.getFileType()==Constants.APP){
                Application.add(items);
            }else if(items.getFileType()==Constants.IMAGE||items.getFileType()==Constants.IMAGE_SELECT){
                Images.add(items);
            }else if(items.getFileType()==Constants.DIRECTORY){
                Directory.add(items);
            }else if(items.getFileType()==Constants.DOCUMENT){
                Document.add(items);
            }else if(items.getFileType()==Constants.AUDIO){
                Audio.add(items);
            }else{
                Others.add(items);
            }
        }

        ArrayList<SelectedItems> selectedItems = new ArrayList<>();

        SelectedItems selectedVideos = new SelectedItems("Videos",Videos);
        SelectedItems selectedApplication = new SelectedItems("Application",Application);
        SelectedItems selectedImages = new SelectedItems("Images",Images);
        SelectedItems selectedDirectory = new SelectedItems("Directory",Directory);
        SelectedItems selectedDocument = new SelectedItems("Document",Document);
        SelectedItems selectedAudio = new SelectedItems("Audio",Audio);
        SelectedItems selectedOthers = new SelectedItems("Others",Others);

        selectedItems.add(selectedVideos);
        selectedItems.add(selectedApplication);
        selectedItems.add(selectedImages);
        selectedItems.add(selectedDirectory);
        selectedItems.add(selectedDocument);
        selectedItems.add(selectedAudio);
        selectedItems.add(selectedOthers);


        RecyclerView recyclerView = dialog.findViewById(R.id.selectedItems);
        TextView tvNoItems = dialog.findViewById(R.id.tvNoItems);
        TextView tvClear = dialog.findViewById(R.id.tvClear);

        MainSelectedAdapter adapter = new MainSelectedAdapter(selectedItems,getApplicationContext(),fileAdapter);
        recyclerView.setAdapter(adapter);

        tvNoItems.setText("Selected Items ("+Integer.toString(selectedFile.size())+")");

        tvClear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                selectedFile.clear();
                refreshBtnStatus();
                fileAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
    }
}