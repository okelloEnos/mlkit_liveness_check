package com.google.mlkit.vision.demo.liveness.listener;


import com.google.mlkit.vision.demo.liveness.entity.LivenessItem;

public interface PrivyCameraLivenessCallBackListener {

    void success(LivenessItem livenessItem);

    void failed(Throwable t);
}


