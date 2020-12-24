package com.example.share.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.share.Model.FileItems;
import com.example.share.Model.SelectedItems;
import com.example.share.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MainSelectedAdapter extends RecyclerView.Adapter<MainSelectedAdapter.ViewHolder>
{
    ArrayList<SelectedItems> items;
    private Context mContext;
    private FileAdapter fileAdapter;

    public MainSelectedAdapter(ArrayList<SelectedItems> items,Context mContext,FileAdapter fileAdapter)
    {
        this.items = items;
        this.mContext = mContext;
        this.fileAdapter = fileAdapter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.parent_item_recycler,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        SelectedItems selectedItems = items.get(position);
        String sectionName = selectedItems.getSectionName();
        ArrayList<FileItems> sectionItems = selectedItems.getSectionItems();

        if(sectionItems.size()>0)
        {
            ChildSelectedAdapter adapter = new ChildSelectedAdapter(sectionItems,mContext,fileAdapter);
            holder.childRecycler.setAdapter(adapter);
            holder.childTitle.setText(sectionName);
        }
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        public RecyclerView childRecycler;
        public TextView childTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            childRecycler =itemView.findViewById(R.id.childRecycler);
            childTitle = itemView.findViewById(R.id.childTitle);
        }
    }
}
