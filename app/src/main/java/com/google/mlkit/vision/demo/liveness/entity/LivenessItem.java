package com.google.mlkit.vision.demo.liveness.entity;

import android.graphics.Bitmap;

public class LivenessItem {

    private Bitmap imageBitmap;
    private String dimensionsInfo;

    public Bitmap getImageBitmap() {
        return this.imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public String getDimensionsInfo() {
        return dimensionsInfo;
    }

    public void setDimensionsInfo(String dimensionsInfo) {
        this.dimensionsInfo = dimensionsInfo;
    }
}
