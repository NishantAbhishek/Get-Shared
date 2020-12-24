package com.example.share.Adapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.share.Helper.Constants;
import com.example.share.MainActivity;
import com.example.share.Model.FileItems;
import com.example.share.R;

import java.util.ArrayList;
import java.util.ListIterator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChildSelectedAdapter extends RecyclerView.Adapter<ChildSelectedAdapter.ViewHolder>
{
    private ArrayList<FileItems> fileItems;
    private Context mContext;
    private FileAdapter fileAdapter;

    public ChildSelectedAdapter(ArrayList<FileItems> fileItems,Context mContext,FileAdapter fileAdapter)
    {
        this.fileItems = fileItems;
        this.mContext = mContext;
        this.fileAdapter =fileAdapter;
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
    public int getItemViewType(int position) {
        return fileItems.get(position).getFileType();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position)
    {
        holder.tvName.setText(fileItems.get(position).getName());
        holder.tvSize.setText(fileItems.get(position).getDiskSpace());
        holder.setClickDelete(fileItems.get(position));

        if(getItemViewType(position)== Constants.APP){
            try {

                String APKFilePath = fileItems.get(position).getPath();
                PackageInfo pi = mContext.getPackageManager().getPackageArchiveInfo(APKFilePath, 0);

                pi.applicationInfo.sourceDir = APKFilePath;
                pi.applicationInfo.publicSourceDir = APKFilePath;

                Drawable APKicon = pi.applicationInfo.loadIcon(mContext.getPackageManager());
                String appName = (String)pi.applicationInfo.loadLabel(mContext.getPackageManager());
                holder.imgIcon.setImageDrawable(APKicon);

//                holder.imgIcon.setImageDrawable(fileItems.get(position).getApplicationInfo().loadIcon(mContext.getPackageManager()));
            }catch (NullPointerException e){
                e.printStackTrace();
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_apk_file));
            }
        }else if(getItemViewType(position)==Constants.AUDIO)
        {
            holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_play_buttonb));
        }else if(getItemViewType(position)==Constants.IMAGE  || fileItems.get(position).getFileType()==Constants.IMAGE_SELECT)
        {
            Glide.with(mContext).load(fileItems.get(position).getPath()).into(holder.imgIcon);
        }else if(getItemViewType(position)==Constants.VIDEO){
            holder.play_pause.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(fileItems.get(position).getPath()).into(holder.imgIcon);
        }else if(getItemViewType(position)==Constants.DIRECTORY)
        {
            holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_directory));
            holder.tvSize.setText(fileItems.get(position).getNumberOfItems());
        }else if(getItemViewType(position)==Constants.DOCUMENT){
            String extension = fileItems.get(position).getExtension();

            if(extension.equals("txt")){
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_text));
            }else if(extension.equals("docx")){
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_doc));
            }else if(extension.equals("pdf")){
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_pdf));
            }else if(extension.equals("pptx")||extension.equals("ppt")){
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_ppt));
            }else{
                holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_info));
            }
        }else{
            String extension = fileItems.get(position).getExtension();
            holder.imgIcon.setImageDrawable(mContext.getDrawable(R.drawable.ic_info));
        }
    }

    @Override
    public int getItemCount()
    {
        return fileItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView imgIcon;
        public TextView tvName;
        public TextView tvSize;
        public ImageView imgDelete;
        public ImageView play_pause;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            play_pause = itemView.findViewById(R.id.play_pause);
        }

        public void setClickDelete(final FileItems items){
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.removeSelected(items.getPath());

                    ListIterator ltr = fileItems.listIterator();

                    while (ltr.hasNext()){

                        if(((FileItems)ltr.next()).getPath().equals(items.getPath())){
                            ltr.remove();
                        }
                    }
                    notifyDataSetChanged();
                    fileAdapter.notifyDataSetChanged();
                }
            });
        }
    }

}
