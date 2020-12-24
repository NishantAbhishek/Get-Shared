package com.example.share.Model;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.example.share.Helper.Constants;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;

public class FileItems implements Serializable
{
    public String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
    private String path;
    private int fileType;
    private transient ApplicationInfo applicationInfo;
    private String appName;

    public FileItems()
    {

    }

    public FileItems(String path, ApplicationInfo applicationInfo, int type,String appName)
    {
        this.path = path;
        this.applicationInfo = applicationInfo;
        this.fileType = type;
        this.appName = appName;

    }

    public String getPath()
    {
        return path;
    }

    public void setApplicationInfo(ApplicationInfo applicationInfo){
        this.applicationInfo = applicationInfo;
    }

    public ApplicationInfo getApplicationInfo()
    {
        return applicationInfo;
    }

    public void setPath(String path){
        this.path = path;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType)
    {
        this.fileType = fileType;
    }

    public FileItems(String path, int fileType)
    {
        this.path = path;
        this.fileType = fileType;
    }

    public File getFile(){
        return new File(path);
    }

    public String createdDate()
    {
        File file = new File(path);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Path uriPath = Paths.get(file.toURI());

            try{
                BasicFileAttributes attributes = Files.readAttributes(file.toPath(),BasicFileAttributes.class);
                long createdAt = attributes.creationTime().toMillis();

                return millisToString(createdAt);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public String millisToString(long time)
    {
        boolean isAM = true;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if(hourofday==0){
            hourofday = 12;
        }

        String merediam;

        if(hourofday>12){
            isAM = false;
            merediam = "PM";

        }else{
            isAM = true;
            merediam = "AM";
        }

        if(!isAM){
            hourofday = hourofday - 12;
        }
        return day + " " + months[month] + " " + year ;
        //+ " " + hourofday+ ":" + minute + " " + merediam
    }

    public String getDiskSpace()
    {
        int KB = 1024;
        int MB = KB*KB;
        int GB = MB*KB;

        double size = 0;

        size = new File(path).length();

        Log.e(getPath()+"-------",Double.toString(GB));

        if(size>GB){
            return String.format("%.2f Gb ", (double)size / GB);
        }else if(size<GB&&size>MB){
            return String.format("%.2f Mb ", (double)size / MB);
        }else if(size<MB&&size>KB){
            return String.format("%.2f Kb ", (double)size/ KB);
        }else{
            return String.format("%.2f bytes ", (double)size);
        }
    }

    public String getNumberOfItems(){
        if(new File(path).isDirectory()){
            try
            {
                return Integer.toString(new File(path).list().length)+" Items";
            }catch (NullPointerException e)
            {
                e.printStackTrace();
            }
        }else{
            return getDiskSpace();
        }
        return null;
    }

    public String getName()
    {
        if(fileType==Constants.APP){
            return appName;
        }else {
            return path.substring(path.lastIndexOf("/")+1,path.length());
        }
    }
    public String getExtension(){
        Log.e(path+"----",path.substring(path.lastIndexOf(".")+1,path.length()));
        return path.substring(path.lastIndexOf(".")+1,path.length());
    }
}