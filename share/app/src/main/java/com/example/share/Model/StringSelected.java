package com.example.share.Model;

import java.io.Serializable;

public class StringSelected implements Serializable
{
    private String path;

    public StringSelected(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }


}
