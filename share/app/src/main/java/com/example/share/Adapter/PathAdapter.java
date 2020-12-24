package com.example.share.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.TextView;

import com.example.share.Helper.LoadMore;
import com.example.share.Helper.ResetInterface;
import com.example.share.Helper.addPath;
import com.example.share.Model.FileItems;
import com.example.share.R;

import java.util.Stack;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PathAdapter extends RecyclerView.Adapter<PathAdapter.pathViewHolder> implements addPath, ResetInterface
{
    private Context mContext;
    private static Stack<FileItems> pathData;
    public static PathAdapter pathAdapter;
    private LoadMore loadMore;

    public PathAdapter(Context mContext)
    {
        this.mContext = mContext;
        pathData = new Stack<>();
    }

    public static boolean isPathEmpty()
    {
        if(pathData.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public void popTop()
    {
        pathData.pop();
        if(isPathEmpty()){
            loadMore.loadNextDirectory("/storage/emulated/0/");
        }else{
            loadMore.loadNextDirectory(pathData.peek().getPath());//loads the last directory
        }
        pathAdapter.notifyDataSetChanged();

        Log.e("---------","nishant");

    }

    public void setLoadMore(LoadMore loadMore)
    {
        this.loadMore = loadMore;
    }

    public void setAdapter(PathAdapter pathAdapter)
    {
        PathAdapter.pathAdapter = pathAdapter;
    }


    @NonNull
    @Override
    public pathViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_path,parent,false);
        return new pathViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull pathViewHolder holder, int position)
    {
        holder.textView.setText(pathData.get(position).getName());
    }

    @Override
    public int getItemCount()
    {
        return pathData.size();
    }

    public static void removeAllData()
    {
        pathData.clear();
    }

    @Override
    public void addPath(FileItems path)
    {
        pathData.push(path);
        this.notifyDataSetChanged();
    }

    @Override
    public void resetInterface() {
        pathData.clear();
        pathAdapter.notifyDataSetChanged();
    }

    class pathViewHolder extends RecyclerView.ViewHolder
    {
        public TextView textView;
        public LinearLayout linearLayout;

        public pathViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textView = itemView.findViewById(R.id.textPath);
            linearLayout = itemView.findViewById(R.id.layoutPath);

            linearLayout.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    refreshList(getAdapterPosition());
                }
            });
        }

        public void refreshList(int postionClicked)
        {
            for(int i = pathData.size(); i>postionClicked+1; i--)
            {
                pathData.pop();
            }
            pathAdapter.notifyDataSetChanged();
            loadMore.loadNextDirectory(pathData.peek().getPath());//loads the end directory
        }
    }

}
