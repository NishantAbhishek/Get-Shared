package com.example.share;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import com.example.share.Database.ItemSentDB;
import com.example.share.Helper.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import androidx.annotation.Nullable;

public class ServiceReceive extends Service{
    public static String  TAG = ServiceReceive.class.toString();
    private String filePath;
    Boolean owner;
    private String hostAdd;
    private Socket socket;
    private ServerSocket serverSocket;
    private static OnProgressUpdateListener progressListener;
    private ItemSentDB ItemSentDB;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean serviceStarted;


    public IBinder binder = new MyServiceBinder();

    public class MyServiceBinder extends Binder
    {
        public ServiceReceive getService(){
            return ServiceReceive.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        serviceStarted = true;
        owner =  Boolean.parseBoolean(intent.getStringExtra("owner"));
        hostAdd = intent.getStringExtra("ipAddress");
        filePath ="/storage/emulated/0/Get_Shared/";
        Log.e(TAG,"Receive service has started IP:-  "+hostAdd);
        ItemSentDB = new ItemSentDB(getApplicationContext());

        File file = new File(filePath);
        if(!file.exists())
        {
            file.mkdir();
        }

        Log.e(TAG,"File  has started");

        receiveData();
        //stopSelf();
        return START_NOT_STICKY;
    }

    public void receiveData(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG,"Receive Thread started");
                try{
                    if(owner==true){
                        serverSocket= new ServerSocket(9999);
                        socket = serverSocket.accept();
                        serverSocket.setReuseAddress(true);
//                        serverSocket.bind(new InetSocketAddress(9999));

                    }else if(owner == false){
                        socket = new Socket();
                        socket.bind(null);
                        socket.connect(new InetSocketAddress(hostAdd,8888),3000);
                        Log.e(TAG,"Connection receive done");
                    }
                    progressListener.setFullMax(100);
                    progressListener.setFullMax(100);
                    progressListener.setfileMax(100);
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    FileOutputStream fileOutputStream;
                    long fullCounter = 0;


                    //receive fileSize
                    Integer numberFile = Integer.parseInt(receiveMessage(inputStream));
                    Log.e(TAG,Integer.toString(numberFile));

                    //send true
                    sendMessage(outputStream,"true");
                    //receive totalSize
                    Long byteSizeTotal = Long.parseLong(receiveMessage(inputStream));
                    Log.e(TAG,Long.toString(byteSizeTotal));

                    //send true
                    sendMessage(outputStream,"true");

                    for (int i = 0; i < numberFile; i++)
                    {
                        //receive File size
                        String longSize = receiveMessage(inputStream);
                        Log.e(TAG,"file size:-"+longSize);
                        long fileSize = Long.parseLong(longSize);
                        Log.e(TAG,"file size:-"+fileSize);
                        //send true
                        sendMessage(outputStream,"true");

                        //receive fileName
                        String fileName = receiveMessage(inputStream);
                        progressListener.setFileName(fileName);

                        //send true
                        sendMessage(outputStream,"true");

                        //receive file
                        long counter = 0;
                        byte[] buffer = new byte[4096];
                        int size = 0;

                        fileOutputStream = new FileOutputStream(new File(filePath+fileName));
                        progressListener.increFileProgress(0);
                        while(counter<fileSize)
                        {
                            size = inputStream.read(buffer);
                            fileOutputStream.write(buffer,0,size);
                            counter = counter+size;
                            fullCounter = fullCounter +size;

                            float percent = (float) ((float) counter/(float)fileSize) *100;
                            float totalPercent = (float) ((float)fullCounter/(float)byteSizeTotal) *100;
                            progressListener.increFileProgress((int)percent);
                            progressListener.increFullProgress((int)totalPercent);
                            progressListener.setPercent((int)totalPercent);
                            Log.e(TAG,"counter:-"+counter+" filesize:-"+size);
                        }
                        ItemSentDB.insertFile(filePath+fileName, Constants.RECEIVE_SERVICE);
                        //sendMessage
                        sendMessage(outputStream,"true");
                        Log.e(TAG,"Complete receive cycle");
                    }

                }catch(IOException e)
                {
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

        });
        t.start();
    }

    public static void setOnProgressChangedListener(OnProgressUpdateListener _progressListener)
    {
        progressListener  = _progressListener;
    }


    public interface  OnProgressUpdateListener
    {
        void setPercent(int Percent);
        void setFullMax(int max);
        void setfileMax(int max);
        void increFileProgress(int progress);
        void increFullProgress(int progress);
        void refreshEverthing();
        void setFileName(String fileName);
        void transferComplete(boolean complete);
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
                    Log.e(TAG,"SERVER SOCKET IS CLOSED");
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

    public boolean isServiceRunning(){
        return serviceStarted;
    }

}
