package com.example.share.Fragment;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.share.Adapter.MainItemSentAdapter;
import com.example.share.Database.ItemSentDB;
import com.example.share.Helper.Constants;
import com.example.share.Model.SentItem;
import com.example.share.Model.SentItems;
import com.example.share.R;
import java.util.ArrayList;

public class DataReceived extends Fragment
{
    private RecyclerView recyclerView;
    private ItemSentDB ItemSentDB;
    private ArrayList<SentItem> sentItems;
    private ArrayList<SentItems> listSentItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_data_received, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        sortAllFiles();
        return view;
    }

    public void sortAllFiles(){
        ItemSentDB = new ItemSentDB(getContext());
        sentItems = ItemSentDB.getAllFiles();
        listSentItems = new ArrayList<>();
        int index = 0;
        ArrayList<SentItem> singleListItem = new ArrayList<>();
        String currentTime;
        if(sentItems.size()>0){
            currentTime = sentItems.get(index).getTransferTime();
        }else{
            currentTime ="";
        }
        int fullSize = sentItems.size();
        for (int i = 0; i < sentItems.size(); i++) {
            boolean dataChanged = false;
            while (dataChanged==false){
                if(currentTime.equals(sentItems.get(index).getTransferTime())){
                    Log.e("Total"+sentItems.size()+"curent"+i,"-"+currentTime);
                    if(sentItems.get(index).getSentReceive()== Constants.RECEIVE_SERVICE){
                        singleListItem.add(sentItems.get(index));
                    }
                    index = index+1;
                }else{
                    Log.e("Total"+sentItems.size()+"curent"+i,""+currentTime);
                    SentItems sentItemss = new SentItems(singleListItem,currentTime);
                    listSentItems.add(sentItemss);
                    Log.e("Adding","Adding");
                    singleListItem = new ArrayList<>();

                    if(sentItems.get(index).getSentReceive()== Constants.SEND_SERVICE)
                    {
                        singleListItem.add(sentItems.get(index));
                    }
                    dataChanged = true;
                    index=index+1;
                }
                if(fullSize!=index){
                    i=index;
                }else{
                    break;
                }
            }
            if(fullSize==index){
                Log.e("Total"+sentItems.size()+"curent"+i,""+currentTime);
                SentItems sentItemss = new SentItems(singleListItem,currentTime);
                listSentItems.add(sentItemss);
                break;
            }
            currentTime = sentItems.get(i).getTransferTime();
        }
        Log.e("s",listSentItems.size()+"");
        MainItemSentAdapter adapter = new MainItemSentAdapter(listSentItems,getContext());
        recyclerView.setAdapter(adapter);
    }


}