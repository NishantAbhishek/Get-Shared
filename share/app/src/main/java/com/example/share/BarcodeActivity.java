package com.example.share;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.share.Model.FileItems;
import com.google.zxing.Result;
import java.util.ArrayList;

public class BarcodeActivity extends AppCompatActivity{
    CodeScanner codeScanner;
    CodeScannerView scannerView;

    private ArrayList<FileItems> stringSelected;
    private int CAMERAREQUEST=1234;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
            processEverythin();

        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERAREQUEST);
        }
    }

    public void processEverythin(){
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            stringSelected = (ArrayList<FileItems>) bundle.getSerializable("selectedData");
        }

        scannerView = findViewById(R.id.codeScanner);
        codeScanner = new CodeScanner(this,scannerView);
        codeScanner.startPreview();
        codeScanner.setDecodeCallback(new DecodeCallback(){
            @Override
            public void onDecoded(@NonNull Result result){
                Intent intent = new Intent(BarcodeActivity.this,ActivitySearchWifi_Send.class);
                String resultData = result.getText();

                String macAddress = resultData.substring(0,17);
                String deviceName = resultData.substring(17,resultData.length());

                intent.putExtra("macAddress",macAddress);
                intent.putExtra("deviceName",deviceName);
                Bundle bundle = new Bundle();
                bundle.putSerializable("selectedData",stringSelected);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==CAMERAREQUEST){
            if (grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                processEverythin();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed(){
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            stringSelected = (ArrayList<FileItems>) bundle.getSerializable("selectedData");
        }
        Intent intent = new Intent(BarcodeActivity.this,ActivitySearchWifi_Send.class);
        Bundle bundle2 = new Bundle();
        bundle2.putSerializable("selectedData",stringSelected);
        intent.putExtras(bundle2);
        startActivity(intent);
    }
}

//54:0E:2D:C4:5F:7B