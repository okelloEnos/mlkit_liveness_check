package com.google.mlkit.vision.demo.liveness.common;

public class CameraUtils {

//    public static void setCameraDisplayOrientation(Camera camera, int cameraId) {
//        Camera.CameraInfo info = new Camera.CameraInfo();
//        Camera.getCameraInfo(cameraId, info);
//        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
//        int degrees = 0;
//        int rotation = windowManager.getDefaultDisplay().getRotation();
////        int rotation = getWindowManager().getDefaultDisplay().getRotation();
////        int degrees = 0;
//
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                degrees = 0;
//                break;
//            case Surface.ROTATION_90:
//                degrees = 90;
//                break;
//            case Surface.ROTATION_180:
//                degrees = 180;
//                break;
//            case Surface.ROTATION_270:
//                degrees = 270;
//                break;
//        }
//
//        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;  // compensate the mirror
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//        camera.setDisplayOrientation(result);
//    }
}
