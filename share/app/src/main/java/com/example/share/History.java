package com.example.share;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.share.Adapter.MainItemSentAdapter;
import com.example.share.Database.ItemSentDB;
import com.example.share.Fragment.DataReceived;
import com.example.share.Fragment.DataSent;
import com.example.share.Model.SentItem;
import com.example.share.Model.SentItems;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class History extends AppCompatActivity
{
    BottomNavigationView bottomNavigationView;
    Fragment selectFragment = new DataReceived();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,selectFragment).commit();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId()){
                case R.id.navReceived:
                    selectFragment = new DataReceived();
                    break;
                case R.id.navSent:
                    selectFragment = new DataSent();
                    break;
            }
            if(selectFragment!=null){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,selectFragment).commit();
            }
            return true;
        }
    };

}