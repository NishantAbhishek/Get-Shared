package com.example.share.Model;

import java.util.ArrayList;

public class SelectedItems
{
    private String SectionName;
    private ArrayList<FileItems> sectionItems;

    public SelectedItems(String sectionName, ArrayList<FileItems> sectionItems)
    {
        SectionName = sectionName;
        this.sectionItems = sectionItems;
    }

    public String getSectionName()
    {
        return SectionName;
    }

    public void setSectionName(String sectionName)
    {
        SectionName = sectionName;
    }

    public ArrayList<FileItems> getSectionItems()
    {
        return sectionItems;
    }

    public void setSectionItems(ArrayList<FileItems> sectionItems)
    {
        this.sectionItems = sectionItems;
    }
}
