package com.example.share.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.share.Model.SentItem;
import com.example.share.Model.SentItems;
import com.example.share.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MainItemSentAdapter extends RecyclerView.Adapter<MainItemSentAdapter.ViewHolder>
{
    ArrayList<SentItems> itemSents;
    private Context mContext;

    public MainItemSentAdapter(ArrayList<SentItems> itemSents, Context mContext) {
        this.itemSents = itemSents;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MainItemSentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.parent_item_recycler,parent,false);
        return new MainItemSentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainItemSentAdapter.ViewHolder holder, int position)
    {
        SentItems itemSent = itemSents.get(position);
        String sectionName = itemSent.getNames();
        ArrayList<SentItem> itemSents =  itemSent.getSentItems();
        holder.childTitle.setText(sectionName);

        if(itemSents.size()>0){
            ChildItemSentAdapter itemSentAdapter = new ChildItemSentAdapter(itemSents,mContext);
            holder.childRecycler.setAdapter(itemSentAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return itemSents.size();
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
