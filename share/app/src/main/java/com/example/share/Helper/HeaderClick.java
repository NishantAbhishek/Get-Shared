package com.example.share.Helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.share.Adapter.FileAdapter;
import com.example.share.Adapter.PathAdapter;
import com.example.share.MainActivity;
import com.example.share.R;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderClick implements View.OnClickListener
{
    private Context mContext;
    private Activity mActivity;
    private Drawable headerDrawable;
    private LoadData loadData;
    private FileAdapter fileAdapter;
    private RecyclerView recyclerView;


    private static final int FILE = 0;
    private static final int APK = 1;
    private static final int PDFS = 2;
    private static final int IMAGE = 3;
    private static final int AUDIO = 4;
    private static final int VIDEO = 5;

    private static boolean FILE_SELECTED = false;
    private static boolean APK_SELECTED = false;
    private static boolean PDFS_SELECTED = false;
    private static boolean IMAGE_SELECTED = false;
    private static boolean AUDIO_SELECTED = false;
    private static boolean VIDEO_SELECTED = false;


    public HeaderClick(Activity mActivity, Context mContext, LoadData loadData,FileAdapter fileAdapter,RecyclerView recyclerView)
    {
        this.fileAdapter = fileAdapter;
        this.mActivity = mActivity;
        this.mContext = mContext;
        this.loadData = loadData;
        headerDrawable = mContext.getDrawable(R.drawable.header_selector_click);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onClick(View v)
    {
        Message message = new Message();

        switch (v.getId())
        {
            case R.id.liFile:
                if(!FILE_SELECTED){
                    changeLayoutManager(Constants.LINEARNORMAL);
                    MainActivity.resetMainActivity(true);
                    setAllFalse();
                    message.obj = Constants.DIRECTORY;
                    loadData.handler.sendMessage(message);
                    setDrawable(FILE);
                }
                FILE_SELECTED = true;
                break;
            case R.id.liApk:
                if(!APK_SELECTED){
                    changeLayoutManager(Constants.GRIDAPPS);
                    MainActivity.resetMainActivity(false);
                    setAllFalse();
                    setDrawable(APK);
                    message.obj = Constants.APP;
                    loadData.handler.sendMessage(message);
                }
                APK_SELECTED = true;
                break;
            case R.id.liPdfs:
                if(!PDFS_SELECTED){
                    changeLayoutManager(Constants.LINEARNORMAL);
                    MainActivity.resetMainActivity(false);
                    setAllFalse();
                    setDrawable(PDFS);
                    message.obj = Constants.DOCUMENT;
                    loadData.handler.sendMessage(message);
                }
                PDFS_SELECTED = true;
                break;
            case R.id.liImage:
                if(!IMAGE_SELECTED){
                    changeLayoutManager(Constants.GRIDIMAGE);
                    MainActivity.resetMainActivity(false);
                    setAllFalse();
                    setDrawable(IMAGE);
                    message.obj = Constants.IMAGE_SELECT;
                    loadData.handler.sendMessage(message);
                }
                IMAGE_SELECTED = true;
                break;
            case R.id.liAudio:
                if(!AUDIO_SELECTED){
                    changeLayoutManager(Constants.LINEARNORMAL);
                    MainActivity.resetMainActivity(false);
                    setAllFalse();
                    setDrawable(AUDIO);
                    message.obj = Constants.AUDIO;
                    loadData.handler.sendMessage(message);
                }
                AUDIO_SELECTED = true;
                break;
            case R.id.liVideo:
                if(!VIDEO_SELECTED){
                    changeLayoutManager(Constants.LINEARNORMAL);
                    MainActivity.resetMainActivity(false);
                    setAllFalse();
                    setDrawable(VIDEO);
                    message.obj = Constants.VIDEO;
                    loadData.handler.sendMessage(message);
                }
                VIDEO_SELECTED = true;
                break;
        }

    }

    public void setDrawable(int position)
    {
        LinearLayout[] headerLayouts = {mActivity.findViewById(R.id.liFile),mActivity.findViewById(R.id.liApk),mActivity.findViewById(R.id.liPdfs),mActivity.findViewById(R.id.liImage),mActivity.findViewById(R.id.liAudio), mActivity.findViewById(R.id.liVideo)};
        TextView[] textViews = {mActivity.findViewById(R.id.tvFile),mActivity.findViewById(R.id.tvApk), mActivity.findViewById(R.id.tvPdfs),mActivity.findViewById(R.id.tvImage),mActivity.findViewById(R.id.tvAudio), mActivity.findViewById(R.id.tvVideo)};
        for (int i = 0; i < headerLayouts.length; i++) {
            if(i==position){
                headerLayouts[i].setBackground(mContext.getDrawable(R.drawable.header_selector_click));
                if(Build.VERSION.SDK_INT>=23)
                {
                    textViews[i].setTextColor(mContext.getColor(R.color.colorPrimary));
                }

            }else{
                headerLayouts[i].setBackground(mContext.getDrawable(R.drawable.header_selector_normal));
                if(Build.VERSION.SDK_INT>=23)
                {
                    textViews[i].setTextColor(mContext.getColor(R.color.colorBlack));
                }
            }
        }
    }

    public void setAllFalse(){
        FILE_SELECTED = false;
        APK_SELECTED = false;
        PDFS_SELECTED = false;
        IMAGE_SELECTED = false;
        AUDIO_SELECTED = false;
        VIDEO_SELECTED = false;
        PathAdapter.removeAllData();
        PathAdapter.pathAdapter.notifyDataSetChanged();
    }
    public void changeLayoutManager(int managerType)
    {
        GridLayoutManager gridLayoutManager;
        switch (managerType){
            case Constants.GRIDAPPS:
            case Constants.GRIDIMAGE:
                gridLayoutManager= new GridLayoutManager(mContext,3);
                gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL); // set Horizontal Orientation
                recyclerView.setLayoutManager(gridLayoutManager);
                break;
            case Constants.LINEARNORMAL:
                LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
                layoutManager.setOrientation(RecyclerView.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
                break;
        }
    }
}
