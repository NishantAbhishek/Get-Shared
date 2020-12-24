package com.example.share.Helper;
import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.share.Adapter.FileAdapter;
import com.example.share.Model.FileItems;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import androidx.recyclerview.widget.RecyclerView;

public class FileHelper
{
    public static boolean stopLoading = false;
    private static final String TAG = FileHelper.class.toString();
    private Stack<String> pathStack;
    private ArrayList<FileItems> fileItems;
    private FileAdapter fileAdapter;
    private Activity mActivity;
    private boolean uiNotified;
    private RecyclerView recyclerView;
    boolean uiNotified2 = false;

    public FileHelper(FileAdapter fileAdapter, Activity activity,ArrayList<FileItems> fileItems,RecyclerView recyclerView)
    {
        this.fileItems = fileItems;
        this.mActivity = activity;
        this.fileAdapter = fileAdapter;
        this.recyclerView = recyclerView;
        pathStack = new Stack<>();
    }

    public String getCurrentDirectory(){
        String currentDirectory = pathStack.peek();
        return currentDirectory;
    }

    public void getNextDir(String path)
    {
        pathStack.push(path);
        fileItems.clear();
        if(!stopLoading){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    fileAdapter.notifyDataSetChanged();
                }
            });
        }
        send_list();
    }

    public void setStartDir(){
//        String path = "/storage/emulated/0/";
        pathStack.clear();
        pathStack.push("/storage/emulated/0/");
        send_list();
    }

    public void lastSavedDir(String path)
    {
        pathStack.clear();
        pathStack.push(path);
    }

    public void getPreviousDir()
    {
        int size = pathStack.size();

        if(size>=3){
            pathStack.peek();
        }else if(size==0){
            pathStack.push("/");
        }
    }

    public void send_list()
    {
        File file = new File(pathStack.peek());
        if(file.canRead()&&file.exists())
        {
            ArrayList<String> nHiddenFiles = new ArrayList<>();

            for(String f : file.list())
            {
                if(f.indexOf(".")!=0)
                {
                    nHiddenFiles.add(f);
                }
            }
            Object[] fileContent = nHiddenFiles.toArray();
            Arrays.sort(fileContent,extension);
            String dir = pathStack.peek();

            for(Object a:fileContent)
            {
                if(new File(dir+"/"+(String) a).isDirectory()){
                    fileItems.add(new FileItems(dir+"/"+a,Constants.DIRECTORY));
                    Log.e(TAG,"-----Inside sendList()--");
                }else {
                    addFileExtension(dir+"/"+(String) a);
                    Log.e(TAG,"-----Inside sendList()--");
                }
            }
        }

        if(!stopLoading){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    fileAdapter.notifyDataSetChanged();
                    Log.e(TAG,"-----Inside sendList()--"+fileItems.size());
                }
            });
        }
    }

    private static Comparator extension = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2)
        {
            String ext = null;
            String ext2 = null;

            int ret;
            try {
                ext = o1.substring(o1.indexOf(".")+1,o1.length()).toLowerCase();
                ext2 = o2.substring(o2.indexOf(".")+1,o2.length()).toLowerCase();
            }catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
            ret = ext.compareTo(ext2);
            return ret;
        }
    };

    public void getAllSimpleFile(int extType)
    {
        notifyAdapter(extType);
    }

    private void getSimpleFiles(String path, int extType)
    {
        File files =new File(path);
        if(files.isDirectory()&&files.canRead()&&files.exists())
        {
            String[] arrFiles = files.list();

            for (int i = 0; i < arrFiles.length; i++)
            {
                File tempFile = new File(files.getAbsolutePath()+"/"+arrFiles[i]);

                if(tempFile.isDirectory() && tempFile.exists() && tempFile.canRead())
                {
                    getSimpleFiles(tempFile.getAbsolutePath(),extType);

                }else if(tempFile.isFile()&&tempFile.canRead()&&tempFile.exists())
                {
                    checkExtension(tempFile.getAbsolutePath(),extType);
                }
            }

        }
        else if(files.isFile()&&files.canRead()&&files.exists())
        {
            checkExtension(files.getAbsolutePath(),extType);
        }
    }

    private void checkExtension(String fullPath,int extType)
    {
        if(fullPath.contains(".")&&fullPath.indexOf(".")!=1){
            String ext = fullPath.substring(fullPath.indexOf("."),fullPath.length());
            switch (extType)
            {
                case Constants.DOCUMENT:
                    if(fullPath.contains(".pdf")||fullPath.contains(".PDF")||fullPath.contains(".docx")||fullPath.contains(".DOCX")||fullPath.contains(".ppt")
                            ||fullPath.contains(".PPT")||fullPath.contains(".txt")||fullPath.contains(".TXT")||fullPath.contains(".word")||fullPath.contains(".WORD")){
                        fileItems.add(new FileItems(fullPath,Constants.DOCUMENT));
                    }
                    break;
                case Constants.IMAGE_SELECT:

                    if(fullPath.contains(".jpg")||fullPath.contains(".JPG")||fullPath.contains(".png")||fullPath.contains(".PNG")||fullPath.contains(".gif")
                            ||fullPath.contains(".GIF")||fullPath.contains(".jpeg")||fullPath.contains(".JPEG"))
                    {
                        Log.e(TAG,fullPath);
                        fileItems.add(new FileItems(fullPath,Constants.IMAGE_SELECT));
                    }
                    break;
                case Constants.VIDEO:
                     if(fullPath.contains(".mp4")||fullPath.contains(".MP4")||fullPath.contains(".mov")||fullPath.contains(".MOV")
                             ||fullPath.contains(".wmv")||fullPath.contains(".WMV")||fullPath.contains("avi")||fullPath.contains(".AVI"))
                     {
                         fileItems.add(new FileItems(fullPath,Constants.VIDEO));
                     }
                    break;
                case Constants.AUDIO:
                     if(ext.equals(".mp3")||ext.equals(".wav")||ext.equals(".aiff")||ext.equals(".au")||ext.equals(".m4a")||ext.equals(".amr"))
                     {
                         fileItems.add(new FileItems(fullPath,Constants.AUDIO));
                     }
                    break;

            }
        }

        if(!stopLoading){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    fileAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void notifyAdapter(int extType)
    {
        if(stopLoading==true){
            uiNotified2 = false;
        }

        while (uiNotified2==false)
        {
            try
            {
                Thread.sleep(1);
                mActivity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(stopLoading==true){
                            fileItems.clear();
                            recyclerView.removeAllViews();
                            fileAdapter.notifyDataSetChanged();
                            uiNotified2 = true;
                            stopLoading=false;
                            Log.e(TAG,"Ui has been notified");
                        }
                    }
                });

                Log.e(TAG,Boolean.toString(stopLoading));

            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        if(stopLoading==false)
        {
            if(extType==Constants.DIRECTORY||extType==Constants.HOMEDIRECTORY)
            {
                setStartDir();
            }else if(extType==Constants.APP){
                getAllTheApps();
            }
            else{
                getSimpleFiles("/storage/emulated/0/",extType);
            }
        }
    }

    private void addFileExtension(String path)
    {
        if(path.contains("."))
        {
            String ext = path.substring(path.indexOf("."),path.length());
            if(ext.equals(".pdf")||ext.equals(".docx")||ext.equals(".pptx")||ext.equals(".txt")||ext.equals(".word")||ext.equals(".ppt")){
                fileItems.add(new FileItems(path,Constants.DOCUMENT));
            }else if(ext.equals(".jpg")||ext.equals(".png")||ext.equals(".gif")||ext.equals(".jpeg")||ext.equals(".webp")){
                fileItems.add(new FileItems(path,Constants.IMAGE));
            }else if(ext.equals(".mp4")||ext.equals(".mov")||ext.equals(".wmv")||ext.equals(".avi")||ext.equals(".mkv")){
                fileItems.add(new FileItems(path,Constants.VIDEO));
            }else if(ext.equals(".mp3")||ext.equals(".wav")||ext.equals(".aiff")||ext.equals(".au")||ext.equals(".m4a")||ext.equals(".amr")){
                fileItems.add(new FileItems(path,Constants.AUDIO));
            }else{
                fileItems.add(new FileItems(path,Constants.QUESTIONDIRECTORY));
            }
        }
    }

    //get all the apps
    public void getAllTheApps()
    {
        PackageManager packageManager = mActivity.getPackageManager();
        checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA),packageManager);
    }

    private void checkForLaunchIntent(List<ApplicationInfo> list,PackageManager packageManager)
    {
        Log.e("TAGm",Integer.toString(list.size()));
        for(ApplicationInfo applicationInfo:list){
            try {
                if(packageManager.getLaunchIntentForPackage(applicationInfo.packageName)!=null){
                    Log.e(TAG,applicationInfo.name);
                    FileItems items = new FileItems(applicationInfo.publicSourceDir,applicationInfo,Constants.APP,applicationInfo.loadLabel(packageManager).toString());
                    fileItems.add(items);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fileAdapter.notifyDataSetChanged();
            }
        });
    }
}
