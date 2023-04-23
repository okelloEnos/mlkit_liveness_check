package com.google.mlkit.vision.demo.liveness.common;

import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CameraOrientationHelper implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private Camera.CameraInfo cameraInfo;

    private int currentOrientation = 0;

    public CameraOrientationHelper(SensorManager sensorManager, Camera.CameraInfo cameraInfo) {
        this.sensorManager = sensorManager;
        this.cameraInfo = cameraInfo;
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void start() {
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public int getCurrentOrientation() {
        return currentOrientation;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix = new float[16];
        float[] orientation = new float[3];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        SensorManager.getOrientation(rotationMatrix, orientation);
        int orientationDegrees = (int) (Math.toDegrees(orientation[0]) + 360) % 360;
        int cameraOrientationDegrees = getCameraOrientationDegrees();
        currentOrientation = (orientationDegrees + cameraOrientationDegrees) % 360;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private int getCameraOrientationDegrees() {
        int cameraOrientation;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraOrientation = cameraInfo.orientation;
        } else {
            cameraOrientation = (cameraInfo.orientation + 180) % 360;
        }
        return cameraOrientation;
    }
}
