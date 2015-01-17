package com.geosoftware.victor.photovertice;

import android.graphics.Bitmap;

/**
 * Created by Victor on 11/1/15.
 */
public class ImageDataModel {
    private Bitmap bmp;
    private String src;

    public ImageDataModel(Bitmap bitmap, String source){
        bmp = bitmap;
        src = source;
    }


    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }
}
