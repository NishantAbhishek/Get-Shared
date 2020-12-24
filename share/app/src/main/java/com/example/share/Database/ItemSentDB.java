package com.example.share.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.share.Helper.Constants;
import com.example.share.Model.SentItem;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.Nullable;

public  class ItemSentDB extends SQLiteOpenHelper
{
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_FILENAME ="filename";
    public static final String COLUMN_FILEPATH ="filepath";
    public static final String COLUMN_TRANSFER_TIME ="transfertime";
    public static final String COLUMN_SENTRECEIVE = "sentreceive";
    public static final String TABLE_NAME = "filesent";
    public static final String DATABASE_NAME = "FileSent.db";
    public static final String TAG = ItemSentDB.class.toString();

    public ItemSentDB(@Nullable Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String createTable ="CREATE TABLE "+TABLE_NAME+" ("+COLUMN_ID+" INTEGER "+" PRIMARY KEY"+" AUTOINCREMENT"+" , "+ COLUMN_FILENAME+" TEXT , "+ COLUMN_FILEPATH+" TEXT ,"+COLUMN_TRANSFER_TIME+" TEXT , "+COLUMN_SENTRECEIVE+" INTEGER )";
        Log.e(TAG,createTable);
        db.execSQL(createTable);
    }

    public boolean insertFiles(ArrayList<String> filePaths ,int sentReceive)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        for (String filePath :filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf("/")+1,filePath.length());
            ContentValues values = new ContentValues();
            values.put(COLUMN_FILENAME,fileName);
            values.put(COLUMN_FILEPATH,filePath);
            values.put(COLUMN_TRANSFER_TIME,millisToString(System.currentTimeMillis()));
            values.put(COLUMN_SENTRECEIVE,sentReceive);
            Log.e(TAG,fileName);
            db.insert(TABLE_NAME,null,values);
        }
        return true;
    }

    public boolean insertFile(String filePath ,int sentReceive){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String fileName = filePath.substring(filePath.lastIndexOf("/")+1,filePath.length());
        contentValues.put(COLUMN_FILENAME,fileName);
        contentValues.put(COLUMN_FILEPATH,filePath);
        contentValues.put(COLUMN_TRANSFER_TIME,millisToString(System.currentTimeMillis()));
        contentValues.put(COLUMN_SENTRECEIVE,sentReceive);
        Log.e(TAG,filePath);
        db.insert(TABLE_NAME,null,contentValues);
        return true;
    }

    public ArrayList<SentItem> getAllFiles(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<SentItem> fileItems = new ArrayList<>();

        String Query = "SELECT * FROM "+TABLE_NAME;
        Cursor res = db.rawQuery(Query,null);

        while (res.moveToNext()){
            fileItems.add(new SentItem(res.getString(res.getColumnIndex(COLUMN_FILENAME)), res.getString(res.getColumnIndex(COLUMN_FILEPATH)), res.getString(res.getColumnIndex(COLUMN_TRANSFER_TIME)), res.getInt(res.getColumnIndex(COLUMN_SENTRECEIVE))));
        }
        return fileItems;
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
        return day + " " + Constants.MONTHS[month] + " " + year ;
        //+ " " + hourofday+ ":" + minute + " " + merediam
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
