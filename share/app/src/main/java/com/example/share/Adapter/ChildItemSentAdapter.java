package com.example.share.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.share.Helper.Constants;
import com.example.share.Model.SentItem;
import com.example.share.R;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class ChildItemSentAdapter extends RecyclerView.Adapter<ChildItemSentAdapter.ViewHolder>
{
    private ArrayList<SentItem> sentItems;
    private Context mContext;

    public ChildItemSentAdapter(ArrayList<SentItem> sentItems, Context mContext) {
        this.sentItems = sentItems;
        this.mContext = mContext;
    }

    @Override
    public int getItemViewType(int position)
    {
        String ext = sentItems.get(position).getExtension();
        String fullPath = sentItems.get(position).getFilePath();
        if(fullPath.contains(".pdf")||fullPath.contains(".PDF")||
                fullPath.contains(".docx")||fullPath.contains(".DOCX")||fullPath.contains(".ppt")
                ||fullPath.contains(".PPT")||fullPath.contains(".txt")||fullPath.contains(".TXT")||fullPath.contains(".word")||fullPath.contains(".WORD")){
            return Constants.DOCUMENT;
        }else if(fullPath.contains(".jpg")||fullPath.contains(".png")||fullPath.contains(".gif")||fullPath.contains(".jpeg")||fullPath.contains(".webp")){
            return Constants.IMAGE;
        }else if(fullPath.contains(".mp4")||fullPath.contains(".mov")||fullPath.contains(".wmv")||fullPath.contains(".avi")||fullPath.contains(".mkv")){
            return Constants.VIDEO;
        }else if(fullPath.contains(".mp3")||fullPath.contains(".wav")||fullPath.contains(".aiff")||fullPath.contains(".au")||fullPath.contains(".m4a")||fullPath.contains(".amr")){
            return Constants.AUDIO;
        }else if(fullPath.contains(".apk")){
            return Constants.APP;
        }
        else{
            return Constants.EMPTY;
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.selected_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        holder.tvName.setText(sentItems.get(position).getFileName());
        holder.tvSize.setText(sentItems.get(position).getDiskSpace());
        switch (getItemViewType(position)){
            case Constants.DOCUMENT:
                String extension = sentItems.get(position).getExtension();
                if(extension.equals("txt")){
                    holder.layoutOnClick(sentItems.get(position),"text/*");
                    holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_text));
                }else if(extension.equals("docx")){
                    holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_doc));
                }else if(extension.equals("pdf")){
                    holder.layoutOnClick(sentItems.get(position),"application/pdf");
                    holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_pdf));
                }else if(extension.equals("pptx")||extension.equals("ppt")){
                    holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_ppt));
                }else{
                    holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_info));
                }
                break;
            case Constants.IMAGE:
                Glide.with(mContext).load(sentItems.get(position).getFilePath()).into(holder.imgIcon);
                holder.layoutOnClick(sentItems.get(position),"image/*");
                break;
            case Constants.VIDEO:
                holder.play_pause.setVisibility(View.VISIBLE);
                holder.layoutOnClick(sentItems.get(position),"video/*");
                Glide.with(mContext).load(sentItems.get(position).getFilePath()).into(holder.imgIcon);
                break;
            case Constants.AUDIO:
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_play_buttonb));
                holder.layoutOnClick(sentItems.get(position),"audio/*");
                break;
            case Constants.APP:
                try {
                    String APKFilePath = sentItems.get(position).getFilePath();
                    PackageInfo pi = mContext.getPackageManager().getPackageArchiveInfo(APKFilePath, 0);

                    String type = "application/vnd.android.package-archive";

                    pi.applicationInfo.sourceDir = APKFilePath;
                    pi.applicationInfo.publicSourceDir = APKFilePath;

                    Drawable APKicon = pi.applicationInfo.loadIcon(mContext.getPackageManager());
                    String appName = (String)pi.applicationInfo.loadLabel(mContext.getPackageManager());
                    holder.imgIcon.setImageDrawable(APKicon);

                    holder.layoutOnClick(sentItems.get(position),type);

//                holder.imgIcon.setImageDrawable(fileItems.get(position).getApplicationInfo().loadIcon(mContext.getPackageManager()));
                }catch (NullPointerException e){
                    e.printStackTrace();
                    holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_apk_file));
                }
                break;
            case Constants.EMPTY:
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_info));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return sentItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView imgIcon;
        public TextView tvName;
        public TextView tvSize;
        public ImageView imgDelete;
        public ImageView play_pause;
        public LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            play_pause = itemView.findViewById(R.id.play_pause);
            layout = itemView.findViewById(R.id.layout);
            imgDelete.setVisibility(View.GONE);
        }

        public void layoutOnClick(final SentItem item,final String textType)
        {
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = item.getFilePath();
                    File file = new File(path);
                    if(file.exists())
                    {
                        startFileIntent(textType,file);
                    }else{
                        Toast.makeText(mContext,"FilePath not found",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        public void startFileIntent(String textType,File file)
        {
            Intent i = new Intent();
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                Uri apkUri = FileProvider.getUriForFile(mContext.getApplicationContext(),mContext.getPackageName()+".provider",file);
                i.setDataAndType(apkUri,textType);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }else{
                i.setDataAndType(Uri.fromFile(file),textType);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            i.setAction(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }
    }


}
