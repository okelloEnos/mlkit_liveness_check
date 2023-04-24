/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mlkit.vision.demo.java;

import static kotlin.random.RandomKt.Random;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.vision.demo.BitmapUtils;
import com.google.mlkit.vision.demo.CameraSource;
import com.google.mlkit.vision.demo.CameraSourcePreview;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.R;
import com.google.mlkit.vision.demo.java.facedetector.FaceDetectorProcessor;
import com.google.mlkit.vision.demo.java.facedetector.Motion;
import com.google.mlkit.vision.demo.liveness.LivenessApp;
import com.google.mlkit.vision.demo.liveness.common.Constant;
import com.google.mlkit.vision.demo.liveness.common.MyApplication;
import com.google.mlkit.vision.demo.liveness.common.PermissionUtil;
import com.google.mlkit.vision.demo.liveness.event.LivenessEventProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Live preview demo for ML Kit APIs. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity
    implements OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {
  private static final String FACE_DETECTION = "Face Detection";

  private static final String TAG = "LivePreviewActivity";
  // Challenge state
  public static boolean isChallengeDone = false;

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = FACE_DETECTION;

  private boolean success = false;

  private String successText;

  private boolean isDebug = false;

  private boolean passedCameraSide = false;

  private String[] motionInstructions;

  private TextView instructions;

  private TextView motionInstruction;

  private FaceDetectorProcessor visionDetectionProcessor;

  int height = 0;

    int width = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_vision_live_preview);
    new MyApplication().setChallengeDone(false);
instructions = findViewById(R.id.instructions);
motionInstruction = findViewById(R.id.motionInstruction);
    preview = findViewById(R.id.preview_view);
    visionDetectionProcessor = new FaceDetectorProcessor(this);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    if (getIntent().getExtras() != null) {
      Bundle b = getIntent().getExtras();
              successText = b.getString(Constant.Keys.SUCCESS_TEXT, getString(R.string.success_text));
      isDebug = b.getBoolean(Constant.Keys.IS_DEBUG, false);
      passedCameraSide = b.getBoolean("camera", false);
      instructions.setText(b.getString(Constant.Keys.INSTRUCTION_TEXT, getString(R.string.instructions)));
      motionInstructions = b.getStringArray(Constant.Keys.MOTION_INSTRUCTIONS);
    }

    if (PermissionUtil.with(this).isCameraPermissionGranted()) {
      createCameraSource(selectedModel);
      startHeadShakeChallenge();
    }
    else {
      PermissionUtil.requestPermission(this, 1, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }

    List<String> options = new ArrayList<>();
    options.add(FACE_DETECTION);

    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
    facingSwitch.setOnCheckedChangeListener(this);

    createCameraSource(selectedModel);

    LivenessEventProvider.INSTANCE.getEventLiveData().observe(this, new Observer<LivenessEventProvider.LivenessEvent>() {
      @Override
      public void onChanged(LivenessEventProvider.LivenessEvent livenessEvent) {
        LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
        LivenessEventProvider.LivenessEvent.Type type = event.getType();


         if(livenessEvent != null){
           switch (livenessEvent.getType()) {
             case HeadShake:
               onHeadShakeEvent();
               break;
             case Default:
               onDefaultEvent();
               break;
           }
         }
        }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    createCameraSource(selectedModel);
  }

  @Override
  public synchronized void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selectedModel = parent.getItemAtPosition(pos).toString();
    Log.d(TAG, "Selected model: " + selectedModel);
    preview.stop();
    createCameraSource(selectedModel);
    startCameraSource();
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    // Do nothing.
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    Log.d(TAG, "Set facing");
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
      } else {
        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
      }
    }
    preview.stop();
    startCameraSource();
  }

  private void createCameraSource(String model) {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }

    Motion motion = Motion.values()[new Random().nextInt(Motion.values().length)];

    switch (motion) {
      case Left:
        motionInstruction.setText(this.motionInstructions[0]);
        break;
      case Right:
        motionInstruction.setText(this.motionInstructions[1]);
        break;
      }
//
//            Motion.Up -> {
//                Toast.makeText(this, "Look Up", Toast.LENGTH_SHORT).show()
//            }
//
//            Motion.Down -> {
//                Toast.makeText(this, "Look Down", Toast.LENGTH_SHORT).show()
//            }


//    visionDetectionProcessor = VisionDetectionProcessor()
    visionDetectionProcessor.isSimpleLiveness(true, this, motion);
    visionDetectionProcessor.isDebugMode(isDebug);

//    cameraSource!!.setMachineLearningFrameProcessor(visionDetectionProcessor);

    try {
      Log.i(TAG, "Using Face Detector Processor");
//      cameraSource.setMachineLearningFrameProcessor(visionDetectionProcessor);
      cameraSource.setMachineLearningFrameProcessor(new FaceDetectorProcessor(this));
    } catch (RuntimeException e) {
      Log.e(TAG, "Can not create image processor: " + model, e);
      Toast.makeText(
              getApplicationContext(),
              "Can not create image processor: " + e.getMessage(),
              Toast.LENGTH_LONG)
          .show();
    }
  }


  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    updateChallenge(false);
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);
//        new Handler().postDelayed(
//                new Runnable() {
//                  @Override
//                  public void run() {
//                    cameraSource.release();
//                    LivenessApp.setCameraResultData(null);
//                    finish();
//                  }
//                }, 100000000
//        );
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createCameraSource(selectedModel);
    startCameraSource();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
    LivenessEventProvider.INSTANCE.getEventLiveData().postValue(null);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  private void navigateBack(boolean success, Bitmap bitmap) {
    if (bitmap != null) {
      if (success) {
        //todo: update the method to uncommented one
//        // get
//        val s: Int = (this.application as MyApplication).cameraFacingSide
//        LivenessApp.setCameraResultData(BitmapUtils.processBitmap(bitmap, s));
        int cameraFacing = cameraSource.getCameraFacing();
        String info = "Camera Side : " + cameraFacing + " The Dimensions of the image are: width: " + bitmap.getWidth() + " : height: " + bitmap.getHeight();
        String cameraInfo = "Camera Dimensions are: width: " + width + " : height: " + height;
                LivenessApp.setCameraResultData(BitmapUtils.rotateBitmapImage(bitmap, height, width, cameraFacing), "Details: " + info + " \n " + cameraInfo);

        finish();
      }
      else {
        LivenessApp.setCameraResultData(null, "0");
        finish();
      }
    }
  }

  private void startHeadShakeChallenge() {
    visionDetectionProcessor.setVerificationStep(1);
  }

  private void onHeadShakeEvent() {
    if (!success) {
      success = true;
      motionInstruction.setText(successText) ;

      updateChallenge(true);
      visionDetectionProcessor.setChallengeDone(true);
    }
  }

  private void onDefaultEvent() {
    if (success) {

      cameraSource.takePicture(null, new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
          Camera.Parameters pm = camera.getParameters();
            width = pm.getPictureSize().width;
           height = pm.getPictureSize().height;

          Log.d(TAG, "onPictureTaken: width: " + width + " height: " + height);
          navigateBack(true, BitmapFactory.decodeByteArray(data, 0, data.length));
        }

//        @Override
//        public void onPictureTaken(byte[] bytes, CameraSource cameraSource) {
//          navigateBack(true, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
//        }
      });
//      new Handler().postDelayed(new Runnable() {
//        @Override
//        public void run() {
//          {
//            LivenessApp.setCameraResultData(null);
//            finish();
////            android.hardware.Camera.PictureCallback pictureCallback = new android.hardware.Camera.PictureCallback() {
////              @Override
////              public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
////                // process the image data here
////                navigateBack(true, BitmapFactory.decodeByteArray(data, 0, data.length));
////              }
////            };
//
////              cameraSource!!.takePicture(null, com.google.android.gms.vision.CameraSource.PictureCallback {
////        navigateBack(true, BitmapFactory.decodeByteArray(it, 0, it.size))
//
////            cameraSource.takePicture(null, new CameraSource.PictureCallback() {
////              @Override
////              public void onPictureTaken(byte[] bytes, Camera camera){
////                navigateBack(true, BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
////              }
////            });
////
////            cameraSource.takePicture(null, P);
//          }
//        }
//      }, 200);
    }
  }

  // challenge update
  public void updateChallenge(boolean state) {
    isChallengeDone = state;
  }

}
