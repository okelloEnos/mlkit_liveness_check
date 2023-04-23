package com.google.mlkit.vision.demo.liveness;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.java.LivePreviewActivity;
import com.google.mlkit.vision.demo.liveness.common.Constant;
import com.google.mlkit.vision.demo.liveness.entity.LivenessItem;
import com.google.mlkit.vision.demo.liveness.listener.PrivyCameraLivenessCallBackListener;


public class LivenessApp {

    private static PrivyCameraLivenessCallBackListener callback;

    private Context context;

    private Bundle bundle;

    private LivenessApp(Context context, boolean isDebug, boolean cameraPart, String successText, String instructions, String[] motionInstructions) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constant.Keys.IS_DEBUG, isDebug);
        bundle.putBoolean("camera", cameraPart);
        bundle.putString(Constant.Keys.SUCCESS_TEXT, successText);
        bundle.putString(Constant.Keys.INSTRUCTION_TEXT, instructions);
        bundle.putStringArray(Constant.Keys.MOTION_INSTRUCTIONS, motionInstructions);
        this.context = context;
        this.bundle = bundle;
    }

    public void start(PrivyCameraLivenessCallBackListener callback) {
        LivenessApp.callback = callback;
        Intent i = new Intent(context, LivePreviewActivity.class);
        i.putExtras(bundle);
        context.startActivity(i);
    }

    public static void setCameraResultData(Bitmap bitmap){
        if (callback != null) {
            if (bitmap != null) {
                LivenessItem livenessItem = new LivenessItem();
                livenessItem.setImageBitmap(bitmap);
                callback.success(livenessItem);
            }
            else {
                callback.failed(new Throwable("ImageBase Not Found"));
            }
        }
        else {
            callback.failed(new Throwable("Null Callback CameraLiveness"));
        }
    }

    public static class Builder {

        private Context context;

        private boolean isDebug;

        private boolean cameraPart;

        private String successText;

        private String instructions;

        private String leftInstruction, rightInstruction;

        public Builder(Context context) {
            this.context = context;
            this.isDebug = false;
            this.cameraPart = false;
            this.successText = context.getString(R.string.success_text);
            this.successText = context.getString(R.string.instructions);
            this.leftInstruction = context.getString(R.string.motion_instruction_left);
            this.rightInstruction = context.getString(R.string.motion_instruction_right);
        }

        public Builder setDebugMode(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder setCameraPart(boolean cameraPart) {
            this.cameraPart = cameraPart;
            return this;
        }

        public Builder setSuccessText(String successText) {
            this.successText = successText;
            return this;
        }

        public Builder setInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder setMotionInstruction(String leftInstruction, String rightInstruction) {
            this.leftInstruction = leftInstruction;
            this.rightInstruction = rightInstruction;
            return this;
        }

        public LivenessApp build() {
            String[] motionInstructions = new String[]{leftInstruction, rightInstruction};
            return new LivenessApp(context, isDebug,cameraPart, successText, instructions, motionInstructions);
        }

    }

}
