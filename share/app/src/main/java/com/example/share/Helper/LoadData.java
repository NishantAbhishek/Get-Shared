package com.example.share.Helper;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.share.Adapter.FileAdapter;
import com.example.share.Model.FileItems;

import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LoadData extends Thread implements LoadMore
{
    private Activity mActivity;
    public Handler handler;
    private int loadType;
    private FileAdapter adapter;
    private ArrayList<FileItems> fileItems;
    private String nextDirect;
    private RecyclerView recyclerView;
    private FileHelper fileHelper;

    public LoadData(FileAdapter adapter,Activity mActivity,ArrayList<FileItems> fileItems,RecyclerView recyclerView)
    {
        this.mActivity = mActivity;
        this.adapter = adapter;
        this.fileItems = fileItems;
        this.recyclerView = recyclerView;
        fileHelper = new FileHelper(adapter,mActivity,fileItems,recyclerView);

        fileHelper.setStartDir();

    }

    @Override
    public void run()
    {
        Looper.prepare();
        handler = new Handler()
        {
            @Override
            public void handleMessage(@NonNull Message msg)
            {
                loadType = (Integer) msg.obj;
                if(loadType == Constants.NEXTDIRECTORY)
                {
                    FileHelper.stopLoading = false;
                    fileHelper.getNextDir(nextDirect);
                }else if(loadType==Constants.HOMEDIRECTORY)
                {
                    FileHelper.stopLoading = true;
                    fileHelper.getAllSimpleFile(loadType);
                }else  if(loadType==Constants.APP){
                    FileHelper.stopLoading = true;
                    fileHelper.getAllSimpleFile(Constants.APP);
                }else if(loadType==Constants.IMAGE_SELECT){
                    FileHelper.stopLoading = true;
                    fileHelper.getAllSimpleFile(Constants.IMAGE_SELECT);
                }
                else{
                    FileHelper.stopLoading = true;
                    fileHelper.getAllSimpleFile(loadType);
                }
            }
        };
        Looper.loop();
    }

    @Override
    public void loadNextDirectory(String parentDirect)
    {
        this.nextDirect = parentDirect;
        Message message = new Message();
        message.obj = Constants.NEXTDIRECTORY;
        handler.sendMessage(message);
    }
}
