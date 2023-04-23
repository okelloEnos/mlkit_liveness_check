package com.google.mlkit.vision.demo.liveness.common;

import android.app.Application;

import androidx.multidex.MultiDexApplication;

public class MyApplication extends MultiDexApplication {

    private int facing;
    private boolean isChallengeDone;

    public int getCameraFacingSide() {
        return facing;
    }

    public void setCameraFacingSide(int facing) {
        this.facing = facing;
    }

    public boolean isChallengeDone() {
        return isChallengeDone;
    }

    public void setChallengeDone(boolean isChallengeDone) {
        this.isChallengeDone = isChallengeDone;
    }
}