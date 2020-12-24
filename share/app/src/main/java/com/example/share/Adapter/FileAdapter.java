package com.example.share.Adapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.share.Helper.Constants;
import com.example.share.Helper.LoadMore;
import com.example.share.MainActivity;
import com.example.share.Model.FileItems;
import com.example.share.R;
import java.io.File;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    public static PathAdapter pathAdapter;
    private static Context mContext;
    private ArrayList<FileItems> fileItems;
    private static LoadMore loadMoreInterface;
    public static String TAG = FileAdapter.class.toString();

    @Override
    public int getItemViewType(int position)
    {
        if(fileItems.size()==0)
        {
            return Constants.EMPTY;
        }
        return fileItems.get(position).getFileType();
    }

    public void setNewList(ArrayList<FileItems> fileItems)
    {
        this.fileItems = fileItems;
        this.notifyDataSetChanged();
    }

    public  FileAdapter(Context mContext,ArrayList<FileItems> fileItems)
    {
        this.mContext = mContext;
        this.fileItems = fileItems;
    }

    public void setFileAdapter(PathAdapter pathAdapt){
        pathAdapter = pathAdapt;
    }

    public void setLoadMore(LoadMore loadMore)
    {
        loadMoreInterface = loadMore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView;

        RecyclerView.ViewHolder vh;

        if(viewType==Constants.IMAGE||viewType==Constants.AUDIO||viewType==Constants.DOCUMENT||viewType==Constants.DIRECTORY||viewType==Constants.QUESTIONDIRECTORY){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pdf,parent,false);
            vh = new simpleChildFile(itemView);
        }else if(viewType==Constants.VIDEO){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video,parent,false);
            vh = new videoHolder(itemView);
        }else  if(viewType==Constants.APP||viewType==Constants.IMAGE_SELECT){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item,parent,false);
            vh = new appViewHolder(itemView);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressing,parent,false);
            vh = new EmptyViewHolder(itemView);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position)
    {
        if(getItemViewType(position)==Constants.DOCUMENT)
        {
            Drawable[] wordIcons = {mContext.getDrawable(R.drawable.ic_pdf),mContext.getDrawable(R.drawable.ic_doc),mContext.getDrawable(R.drawable.ic_text),mContext.getDrawable(R.drawable.ic_ppt)};
            ((simpleChildFile)holder).setData(fileItems.get(position),wordIcons);
        }else if(getItemViewType(position)==Constants.AUDIO)
        {
            Drawable[] wordIcons = {mContext.getDrawable(R.drawable.ic_play_buttonb)};
            ((simpleChildFile)holder).setData(fileItems.get(position),wordIcons);
        }else if(getItemViewType(position)==5){
        }else if(getItemViewType(position)==Constants.DIRECTORY){
            Drawable[] Icons = {mContext.getDrawable(R.drawable.ic_directory)};
            ((simpleChildFile)holder).setData(fileItems.get(position),Icons);
        }else if(getItemViewType(position)==Constants.IMAGE){
            Drawable[] Icons = {mContext.getDrawable(R.drawable.ic_gallery)};
            ((simpleChildFile)holder).setData(fileItems.get(position),Icons);
        }else if(getItemViewType(position)==Constants.VIDEO){
            ((videoHolder)holder).setData(fileItems.get(position));
        }else if(getItemViewType(position)==Constants.QUESTIONDIRECTORY){
            Drawable[] Icons = {mContext.getDrawable(R.drawable.ic_info)};
            ((simpleChildFile)holder).setData(fileItems.get(position),Icons);
        }else if(getItemViewType(position)==Constants.IMAGE_SELECT ||getItemViewType(position)==Constants.APP){
            ((appViewHolder)holder).setData(fileItems.get(position));
        }

    }

    @Override
    public int getItemCount()
    {
        if(fileItems==null){
            fileItems = new ArrayList<>();
        }
        return fileItems.size();
    }

    static class simpleChildFile extends RecyclerView.ViewHolder
    {
        private ImageView iconImage;
        private TextView tvName, tvSize, tvCreation;
        private LinearLayout linearItems;
        private CheckBox checkBox;

        public simpleChildFile(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.icon);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvCreation = itemView.findViewById(R.id.tvCreation);
            linearItems = itemView.findViewById(R.id.linear_item);
            checkBox = itemView.findViewById(R.id.checkbox);
        }

        void setData(final FileItems fileItem, Drawable[] drawable)
        {
            tvName.setText(fileItem.getName());
            checkBox.setChecked(MainActivity.isSelected(fileItem));

            checkBox.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if(checkBox.isChecked()){
                        checkBox.setChecked(true);
                        MainActivity.addSelection(fileItem);
                    }else{
                        checkBox.setChecked(false);
                        MainActivity.removeSelected(fileItem.getPath());
                    }
                }
            });

            linearItems.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(!checkBox.isChecked()){
                        checkBox.setChecked(true);
                        MainActivity.addSelection(fileItem);
                    }else{
                        checkBox.setChecked(false);
                        MainActivity.removeSelected(fileItem.getPath());
                    }
                    return false;
                }
            });

            linearItems.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!checkBox.isChecked()){
                        checkBox.setChecked(true);
                        MainActivity.addSelection(fileItem);
                    }else{
                        checkBox.setChecked(false);
                        MainActivity.removeSelected(fileItem.getPath());
                    }
                }
            });


            if(new File(fileItem.getPath()).isDirectory())
            {
                tvSize.setText(fileItem.getNumberOfItems());
                iconImage.setImageDrawable(drawable[0]);//it will give the icon of audio files

                linearItems.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(!MainActivity.isSelected(fileItem)){
                            loadMoreInterface.loadNextDirectory(fileItem.getPath());
                            pathAdapter.addPath(fileItem);
                        }
                    }
                });

                linearItems.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(!checkBox.isChecked()){
                            checkBox.setChecked(true);
                            MainActivity.addSelection(fileItem);
                        }else{
                            checkBox.setChecked(false);
                            MainActivity.removeSelected(fileItem.getPath());
                        }
                        return false;
                    }
                });


            }else if(fileItem.getExtension().equals("txt"))
            {
                tvSize.setText(fileItem.getDiskSpace());
                iconImage.setImageDrawable(drawable[2]);

                linearItems.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        Intent i = new Intent();
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",fileItem.getFile());
                            i.setDataAndType(apkUri,"text/*");
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }else{
                            i.setDataAndType(Uri.fromFile(fileItem.getFile()),"text/*");
                        }
                        i.setAction(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                        return false;
                    }
                });

            }
            else if(fileItem.getExtension().equals("docx"))
            {
                tvSize.setText(fileItem.getDiskSpace());
                iconImage.setImageDrawable(drawable[1]);
            }else if(fileItem.getExtension().equals("pdf"))
            {
                tvSize.setText(fileItem.getDiskSpace());
                iconImage.setImageDrawable(drawable[0]);

                linearItems.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent i = new Intent();
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",fileItem.getFile());
                            i.setDataAndType(apkUri,"application/pdf");
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }else{
                            i.setDataAndType(Uri.fromFile(fileItem.getFile()),"application/pdf");
                        }
                        i.setAction(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                        return false;
                    }
                });


            }else if(fileItem.getExtension().equals("pptx")||fileItem.getExtension().equals("ppt"))
            {
                tvSize.setText(fileItem.getDiskSpace());
                iconImage.setImageDrawable(drawable[3]);

            }else if(fileItem.getFileType()==Constants.AUDIO)
            {
                tvSize.setText(fileItem.getDiskSpace());
                iconImage.setImageDrawable(drawable[0]);//it will give the icon of audio files

                linearItems.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        Intent i = new Intent();
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",fileItem.getFile());
                            i.setDataAndType(apkUri,"audio/*");
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }else{
                            i.setDataAndType(Uri.fromFile(fileItem.getFile()),"audio/*");
                        }
                        i.setAction(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);

                        return false;
                    }
                });


            }else if(fileItem.getFileType()==Constants.IMAGE){
                Glide.with(mContext).load(fileItem.getPath()).into(iconImage);
                tvSize.setText(fileItem.getDiskSpace());


                linearItems.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent i = new Intent();
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",fileItem.getFile());
                            i.setDataAndType(apkUri,"image/*");
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }else{
                            i.setDataAndType(Uri.fromFile(fileItem.getFile()),"image/*");
                        }
                        i.setAction(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                        return false;
                    }
                });

//                linearItems.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        Intent i = new Intent();
//                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
//                        {
//                            Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",fileItem.getFile());
//                            i.setDataAndType(apkUri,"image/*");
//                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        }else{
//                            i.setDataAndType(Uri.fromFile(fileItem.getFile()),"image/*");
//                        }
//                        i.setAction(Intent.ACTION_VIEW);
//                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mContext.startActivity(i);
//                    }
//                });

            }else if(fileItem.getFileType()==Constants.QUESTIONDIRECTORY){
                iconImage.setImageDrawable(drawable[0]);
                tvSize.setText(fileItem.getDiskSpace());
            }
            tvCreation.setText(fileItem.createdDate());
        }


    }

    static class videoHolder extends RecyclerView.ViewHolder
    {
        private ImageView iconImage;
        private TextView tvName, tvSize, tvCreation;
        private LinearLayout linearItems;
        private CheckBox checkBox;

        public videoHolder(@NonNull View itemView) {
            super(itemView);
            iconImage = itemView.findViewById(R.id.icon);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvCreation = itemView.findViewById(R.id.tvCreation);
            linearItems = itemView.findViewById(R.id.linear_item);
            checkBox = itemView.findViewById(R.id.checkbox);
        }

        void setData(final FileItems fileItem)
        {
            Glide.with(mContext).load(fileItem.getPath()).into(iconImage);
            tvSize.setText(fileItem.getDiskSpace());
            tvName.setText(fileItem.getName());
            tvCreation.setText(fileItem.createdDate());

            checkBox.setChecked(MainActivity.isSelected(fileItem));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkBox.isChecked()){
                        checkBox.setChecked(true);
                        MainActivity.addSelection(fileItem);
                    }else{
                        checkBox.setChecked(false);
                        MainActivity.removeSelected(fileItem.getPath());
                    }
                }
            });

            linearItems.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(!checkBox.isChecked()){
                        checkBox.setChecked(true);
                        MainActivity.addSelection(fileItem);
                    }else{
                        checkBox.setChecked(false);
                        MainActivity.removeSelected(fileItem.getPath());
                    }
                }
            });

            linearItems.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent i = new Intent();
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                    {
                        Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",fileItem.getFile());
                        i.setDataAndType(apkUri,"video/*");
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }else{
                        i.setDataAndType(Uri.fromFile(fileItem.getFile()),"video/*");
                    }
                    i.setAction(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(i);
                    return false;
                }
            });

        }
    }

    static class appViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView appImage;
        private TextView appName;
        private CheckBox checkBox;
//        private TextView tvCreation;

        public appViewHolder(@NonNull View itemView)
        {
            super(itemView);
            appImage = itemView.findViewById(R.id.imgImage);
            appName = itemView.findViewById(R.id.tvName);
            checkBox = itemView.findViewById(R.id.checkbox);
//            tvCreation = itemView.findViewById(R.id.tvCreation);
        }

        void setData(final FileItems items)
        {
            if(items.getFileType()==Constants.APP)
            {

                Glide.with(mContext).load(items.getApplicationInfo().loadIcon(mContext.getPackageManager())).into(appImage);
                appName.setText(items.getApplicationInfo().loadLabel(mContext.getPackageManager()));
                checkBox.setChecked(MainActivity.isSelected(items));

                checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(checkBox.isChecked()){
                            checkBox.setChecked(true);
                            MainActivity.addSelection(items);
                            Log.e(TAG,items.getApplicationInfo().publicSourceDir);
                        }else{
                            checkBox.setChecked(false);
                            MainActivity.removeSelected(items.getPath());
                        }
                    }
                });

                appImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!checkBox.isChecked()){
                            checkBox.setChecked(true);
                            MainActivity.addSelection(items);
                        }else{
                            checkBox.setChecked(false);
                            MainActivity.removeSelected(items.getPath());
                        }
                    }
                });
            }else if(items.getFileType()==Constants.IMAGE_SELECT){

                Log.e(TAG,items.getName());
                Glide.with(mContext).load(items.getPath()).into(appImage);
                appName.setText(items.getName());
                checkBox.setChecked(MainActivity.isSelected(items));

                checkBox.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        if(checkBox.isChecked()){
                            checkBox.setChecked(true);
                            MainActivity.addSelection(items);
                        }else{
                            checkBox.setChecked(false);
                            MainActivity.removeSelected(items.getPath());
                        }
                    }
                });

                appImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        if(!checkBox.isChecked()){
                            checkBox.setChecked(true);
                            MainActivity.addSelection(items);
                        }else{
                            checkBox.setChecked(false);
                            MainActivity.removeSelected(items.getPath());
                        }
                    }
                });

                appImage.setOnLongClickListener(new View.OnLongClickListener(){
                    @Override
                    public boolean onLongClick(View v) {
                        Intent i = new Intent();
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                        {
                            Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",items.getFile());
                            i.setDataAndType(apkUri,"image/*");
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }else{
                            i.setDataAndType(Uri.fromFile(items.getFile()),"image/*");
                        }
                        i.setAction(Intent.ACTION_VIEW);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(i);
                        return false;
                    }
                });

            }
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder
    {
        public EmptyViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
}
