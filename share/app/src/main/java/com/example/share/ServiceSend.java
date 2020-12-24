package com.example.share;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.example.share.Database.ItemSentDB;
import com.example.share.Helper.Constants;
import com.example.share.Model.FileItems;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import androidx.annotation.Nullable;

public class ServiceSend extends Service
{
    private static String TAG = ServiceSend.class.toString();
    private ArrayList<FileItems> filePaths;
    Socket socket;
    String hostAdd;
    Boolean owner;
    private ServerSocket serverSocket;
    private static OnProgressUpdateListener progressListener;
    ArrayList<String> filesChoosen = new ArrayList<>();
    private long byteSizeTotal = 0;
    private ItemSentDB ItemSentDB;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean serviceStarted;

    public IBinder binder = new MySeriviceBinder();

    public class MySeriviceBinder extends Binder
    {
        public ServiceSend getService()
        {
            return ServiceSend.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        serviceStarted = true;
        hostAdd = intent.getStringExtra("ipAddress");
        owner =  Boolean.parseBoolean(intent.getStringExtra("owner"));
        Bundle bundle = intent.getExtras();
        filePaths = (ArrayList<FileItems>) bundle.getSerializable("selectedData");
        Log.e(TAG,Integer.toString(filePaths.size()));
        Log.e(TAG,"Sender service has started");
        ItemSentDB = new ItemSentDB(getApplicationContext());
        sendData();
        return START_NOT_STICKY;
    }

    public void sendData()
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    if(owner==true){
                        serverSocket= new ServerSocket(8888);
                        socket = serverSocket.accept();
                        serverSocket.setReuseAddress(true);
//                        serverSocket.bind(new InetSocketAddress(8888));
                    }else if(owner == false){
                        socket = new Socket();
                        socket.bind(null);
                        socket.connect(new InetSocketAddress(hostAdd,9999),3000);
                        Log.e(TAG,"Connection send done");
                    }
                    settingUp();
                    progressListener.setFullMax(100);
                    progressListener.setfileMax(100);
                    progressListener.increFullProgress(0);

                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    FileInputStream fileInputStream;
                    long fullCounter = 0;

                    int fileNumber = filesChoosen.size();

                    //send fileNumber
                    Log.e(TAG,"fileSize:- "+fileNumber);
                    sendMessage(outputStream,Integer.toString(fileNumber));

                    //receivetrue
                    Log.e(TAG,receiveMessage(inputStream));

                    //send totalSize
                    sendMessage(outputStream,Long.toString(byteSizeTotal));

                    //receiveTrue
                    Log.e(TAG,receiveMessage(inputStream));

                    for (int k = 0; k < fileNumber; k++)
                    {

                        String filePath = filesChoosen.get(k);

                        //sendfileSize
                        long fileSize = new File(filePath).length();
                        sendMessage(outputStream,Long.toString(fileSize));

                        //receive true
                        Log.e(TAG,"file size sent:- "+receiveMessage(inputStream));

                        //send filename
                        String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                        sendMessage(outputStream,fileName);
                        progressListener.setFileName(fileName);


                        //receive true
                        Log.e("fileNameReceived",receiveMessage(inputStream));

                        //send file
                        int size = 0;
                        long counter = 0;
                        byte[] buffer = new byte[4096];
                        fileInputStream = new FileInputStream(new File(filePath));

                        progressListener.increFileProgress(0);
                        while (counter<fileSize)
                        {
                            size = fileInputStream.read(buffer);
                            outputStream.write(buffer,0,size);
                            counter = counter+size;
                            fullCounter = fullCounter +size;
                            float percent = (float) ((float) counter/(float)fileSize) *100;
                            float totalPercent = (float) ((float)fullCounter/(float)byteSizeTotal) *100;
                            progressListener.increFileProgress((int)percent);
                            progressListener.increFullProgress((int)totalPercent);
                            progressListener.setPercent((int)totalPercent);
                            Log.e(TAG,"fileSize:- "+fileSize+" counter"+counter+"size:-"+size+" percent:- "+percent+"percent");
                        }
                        ItemSentDB.insertFile(filePath, Constants.SEND_SERVICE);

                        //receive message
                        receiveMessage(inputStream);
                        Log.e(TAG,"Complete Send cycle");
                    }

                }catch(IOException e){
                    e.printStackTrace();
                }finally {
                    try{
                        boolean connectionClosed = isConnectionClosed();
                        do{
                            if(connectionClosed){
                                progressListener.transferComplete(true);
                            }else{
                                connectionClosed = isConnectionClosed();
                            }
                        }while (!connectionClosed);
                        stopSelf();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }

            public void sendMessage(OutputStream outputStream,String message) throws IOException {
                byte[] messagebyte = message.getBytes();
                outputStream.write(messagebyte);
            }

            public String receiveMessage(InputStream inputStream) throws IOException
            {
                byte[] buffer = new byte[4096];
                int messageLength = inputStream.read(buffer);
                return new String(buffer,0,messageLength);
            }

            public void settingUp()
            {
                for(FileItems path:filePaths){
                    getFiles(path.getPath());
                }
            }

            public void getFiles(String path)
            {
                File file = new File(path);
                if(file.isDirectory() && file.exists() && file.canRead()){

                    String[] arrFiles = file.list();

                    for (int i = 0; i < arrFiles.length; i++)
                    {
                        File tempFile = new File(file.getAbsolutePath()+"/"+arrFiles[i]);

                        if(tempFile.isDirectory()&&tempFile.canRead()&&tempFile.exists()){
                            getFiles(tempFile.getAbsolutePath());
                        }else if(tempFile.isFile()&&tempFile.canRead()&&tempFile.exists()&&tempFile.length()>0)
                        {
                            filesChoosen.add(tempFile.getAbsolutePath());
                            byteSizeTotal = tempFile.length()+byteSizeTotal;
                            Log.e(TAG,tempFile.getAbsolutePath());
                            Log.e(TAG,Long.toString(byteSizeTotal));
                        }
                    }
                }else if(file.isFile() && file.canRead()&&file.exists()&&file.length()>0){
                    filesChoosen.add(file.getAbsolutePath());
                    byteSizeTotal = file.length()+byteSizeTotal;
                    Log.e(TAG,Long.toString(byteSizeTotal));
                }
            }

        });



        t.start();
    }

    public boolean isConnectionClosed() throws IOException{
        Log.e(TAG,"isConnectionClosed");
        outputStream.close();
        inputStream.close();
        if(owner==true){
            try{
                if(!socket.isClosed()){
                    serverSocket.close();
                    socket.close();
                    Log.e(TAG,"SERVER SOCKET IS NOT CLOSED");
                    return serverSocket.isClosed();
                }else{
                    Log.e(TAG,"SERVER SOCKET IS  CLOSED");
                    return true;

                }
            }catch (IOException e){
                e.printStackTrace();
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return true;
            }
        }else{
            try {
                if(!socket.isClosed()){
                    socket.close();
                    Log.e(TAG,"SOCKET IS NOT CLOSED");
                    return socket.isClosed();
                }else{
                    Log.e(TAG,"SOCKET IS CLOSED");
                    return true;
                }
            }catch (IOException e) {
                e.printStackTrace();
                return true;
            }
        }
    }

    @Override
    public void onDestroy() {
        serviceStarted = false;
        super.onDestroy();
    }

    public interface  OnProgressUpdateListener
    {
        void setPercent(int Percent);
        void setFullMax(int max);
        void setfileMax(int max);
        void increFileProgress(int progress);
        void increFullProgress(int progress);
        void setFileName(String fileName);
        void transferComplete(boolean complete);
    }

    public static void setOnProgressChangedListener(OnProgressUpdateListener _progressListener)
    {
        progressListener  = _progressListener;
    }

    public boolean isServiceRunning(){
        return serviceStarted;
    }

}
