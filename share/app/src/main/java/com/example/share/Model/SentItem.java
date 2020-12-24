package com.example.share.Model;

import android.util.Log;

import java.io.File;

public class SentItem {
    private String fileName;
    private String filePath;
    private String transferTime;
    private int sentReceive;

    public SentItem(String fileName, String filePath, String transferTime, int sentReceive) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.transferTime = transferTime;
        this.sentReceive = sentReceive;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTransferTime() {
        return transferTime;
    }

    public int getSentReceive() {
        return sentReceive;
    }

    public String getExtension(){
        return filePath.substring(filePath.lastIndexOf(".")+1,filePath.length());
    }

    public String getDiskSpace()
    {
        int KB = 1024;
        int MB = KB*KB;
        int GB = MB*KB;

        double size = 0;

        File file =new File(filePath);
        if(file.exists()){
            size = file.length();
            if(size>GB){
                return String.format("%.2f Gb ", (double)size / GB);
            }else if(size<GB&&size>MB){
                return String.format("%.2f Mb ", (double)size / MB);
            }else if(size<MB&&size>KB){
                return String.format("%.2f Kb ", (double)size/ KB);
            }else{
                return String.format("%.2f bytes ", (double)size);
            }
        }else {
            return "";
        }
//        Log.e(getPath()+"-------",Double.toString(GB));
    }

}
