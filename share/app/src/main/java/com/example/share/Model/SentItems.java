package com.example.share.Model;

import java.util.ArrayList;

public class SentItems
{
    private ArrayList<SentItem> sentItems;
    private String names;

    public SentItems(ArrayList<SentItem> sentItems, String names)
    {
        this.sentItems = sentItems;
        this.names = names;
    }

    public ArrayList<SentItem> getSentItems()
    {
        return sentItems;
    }

    public void setSentItems(ArrayList<SentItem> sentItems)
    {
        this.sentItems = sentItems;
    }

    public String getNames()
    {
        return names;
    }

    public void setNames(String names)
    {
        this.names = names;
    }
}
