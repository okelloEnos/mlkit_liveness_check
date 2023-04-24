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

package com.google.mlkit.vision.demo.java.facedetector;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.demo.GraphicOverlay;
import com.google.mlkit.vision.demo.java.LivePreviewActivity;
import com.google.mlkit.vision.demo.java.VisionProcessorBase;
import com.google.mlkit.vision.demo.liveness.common.DetectionThreshold;
import com.google.mlkit.vision.demo.liveness.common.MyApplication;
import com.google.mlkit.vision.demo.liveness.event.LivenessEventProvider;
import com.google.mlkit.vision.demo.preference.PreferenceUtils;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Face Detector Demo. */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

  private static final String TAG = "FaceDetectorProcessor";

  private final FaceDetector detector;

  private int headState = 0;

  private int state = 0;

  private boolean isMouthOpen = false;

  private int verificationStep = 0;

  private int faceId = -1;

  private boolean isEventSent = false;

  private ArrayList<Integer> challengeOrder = new ArrayList();

  private boolean isSimpleLiveness = false;

  private boolean isDebug = false;

  private boolean isChallengeDone = false;

  private Motion motion;

  public FaceDetectorProcessor(Context context) {
    super(context);
      FaceDetectorOptions faceDetectorOptions = new FaceDetectorOptions.Builder()
              .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
              .setLandmarkMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
              .setClassificationMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
              .setContourMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
              .enableTracking()
              .build();
    Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
    detector = FaceDetection.getClient(faceDetectorOptions);
      motion = Motion.values()[new Random().nextInt(Motion.values().length)];
  }

  @Override
  public void stop() {
    super.stop();
    detector.close();
  }

  @Override
  protected Task<List<Face>> detectInImage(InputImage image) {
    return detector.process(image);
  }


  @Override
  protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
      for (Face face : faces) {

          if (isDebug) {
              graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
          }
          // get
          boolean currentChallengeState =  LivePreviewActivity.isChallengeDone;
          if (currentChallengeState) {
              processDefaultPosition(face);
          }
          else {
              processHeadFacing(face, motion);
          }
      }
  }

  private static void logExtrasForTesting(Face face) {
    if (face != null) {
      Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
      Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

      // All landmarks
      int[] landMarkTypes =
          new int[] {
            FaceLandmark.MOUTH_BOTTOM,
            FaceLandmark.MOUTH_RIGHT,
            FaceLandmark.MOUTH_LEFT,
            FaceLandmark.RIGHT_EYE,
            FaceLandmark.LEFT_EYE,
            FaceLandmark.RIGHT_EAR,
            FaceLandmark.LEFT_EAR,
            FaceLandmark.RIGHT_CHEEK,
            FaceLandmark.LEFT_CHEEK,
            FaceLandmark.NOSE_BASE
          };
      String[] landMarkTypesStrings =
          new String[] {
            "MOUTH_BOTTOM",
            "MOUTH_RIGHT",
            "MOUTH_LEFT",
            "RIGHT_EYE",
            "LEFT_EYE",
            "RIGHT_EAR",
            "LEFT_EAR",
            "RIGHT_CHEEK",
            "LEFT_CHEEK",
            "NOSE_BASE"
          };
      for (int i = 0; i < landMarkTypes.length; i++) {
        FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
        if (landmark == null) {
          Log.v(
              MANUAL_TESTING_LOG,
              "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
        } else {
          PointF landmarkPosition = landmark.getPosition();
          String landmarkPositionStr =
              String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
          Log.v(
              MANUAL_TESTING_LOG,
              "Position for face landmark: "
                  + landMarkTypesStrings[i]
                  + " is :"
                  + landmarkPositionStr);
        }
      }
      Log.v(
          MANUAL_TESTING_LOG,
          "face left eye open probability: " + face.getLeftEyeOpenProbability());
      Log.v(
          MANUAL_TESTING_LOG,
          "face right eye open probability: " + face.getRightEyeOpenProbability());
      Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
      Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
    }
  }

  @Override
  protected void onFailure(@NonNull Exception e) {
    Log.e(TAG, "Face detection failed " + e);
  }

 public void isSimpleLiveness(boolean isSimpleLiveness, Context context, Motion motion) {
    this.isSimpleLiveness = isSimpleLiveness;
    this.motion = motion;
  }

  public  void  isDebugMode(boolean isDebug) {
    this.isDebug = isDebug;
  }

  public void  setVerificationStep(int verificationStep) {
    this.verificationStep = verificationStep;
  }

  public void  setChallengeDone(boolean isChallengeDone) {
    this.isChallengeDone = isChallengeDone;
  }

  /**
   *
   * @param face
   *
   * state 0 for both eyes opened
   * state 1 for one eye closed
   * state 2 for both eyes closed
   */
  private void  processBlink(Face face) {
    Float left = face.getLeftEyeOpenProbability();
    Float right = face.getRightEyeOpenProbability();
   if(left == null || right == null){
      return;
    }

   if(face.getTrackingId() != null){
       if (this.faceId == -1 || face.getTrackingId() == faceId) {
           switch (state) {
               case 0 : if (left > DetectionThreshold.EYE_OPEN_THRESHOLD && right > DetectionThreshold.EYE_OPEN_THRESHOLD) {
                   // Both eyes are initially open
                   state = 1;
               }
break;
               case 1 : if (left < DetectionThreshold.EYE_CLOSED_THRESHOLD && right < DetectionThreshold.EYE_CLOSED_THRESHOLD) {
                   // Both eyes become closed
                   state = 2;
               }
break;
               case 2 : if (left > DetectionThreshold.EYE_OPEN_THRESHOLD && right > DetectionThreshold.EYE_OPEN_THRESHOLD) {
                   // Both eyes are open again
                   Log.i("BlinkTracker", "blink occurred!");
                   LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
                   if (faceId == -1) {
                       this.faceId = face.getTrackingId();
                   }
                   event.setType(LivenessEventProvider.LivenessEvent.Type.Blink);
                   LivenessEventProvider.INSTANCE.post(event);
                   state = 0;
               }
               break;
           }
       }
       else {
           if (!isEventSent) {
               LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
               event.setType(LivenessEventProvider.LivenessEvent.Type.NotMatch);
               LivenessEventProvider.INSTANCE.post(event);
               isEventSent = true;
           }
       }
   }
   else{
       if (!isEventSent) {
           LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
           event.setType(LivenessEventProvider.LivenessEvent.Type.NotMatch);
           LivenessEventProvider.INSTANCE.post(event);
           isEventSent = true;
       }
   }
  }


  /**
   *
   * @param face
   *
   * headState 0 for neutral
   * headState 1 for from left to right
   * headState 2 for from right to left
   */
  private void  processHeadShake(Face face) {
    float headEulerAngleY = face.getHeadEulerAngleY();
    LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
    event.setType(LivenessEventProvider.LivenessEvent.Type.HeadShake);

    if (this.faceId == -1 || face.getTrackingId() == faceId) {
      if (headEulerAngleY <= DetectionThreshold.LEFT_HEAD_THRESHOLD) {
        if (headState != 1) {
          headState = 1;
          LivenessEventProvider.INSTANCE.post(event);
        }
      } else if (headEulerAngleY >= DetectionThreshold.RIGHT_HEAD_THRESHOLD) {
        if (headState != 2) {
          headState = 2;
          LivenessEventProvider.INSTANCE.post(event);
        }
      }
    }
    else {
      if (!isEventSent) {
        event.setType(LivenessEventProvider.LivenessEvent.Type.NotMatch);
        LivenessEventProvider.INSTANCE.post(event);
        isEventSent = true;
      }
    }
  }

  private void  processHeadFacing(Face face, Motion motion) {
    float headEulerAngleY = face.getHeadEulerAngleY();
      float headEulerAngleX = face.getHeadEulerAngleX();
    LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
    event.setType(LivenessEventProvider.LivenessEvent.Type.HeadShake);
    if (this.faceId == -1) {
      switch (motion) {
          case Left:
          Log.e(TAG, "$headEulerAngleY");
          if (headEulerAngleY >= DetectionThreshold.RIGHT_HEAD_FACING_THRESHOLD) {
            LivenessEventProvider.INSTANCE.post(event);
          }
break;
          case Right:
          Log.e(TAG, "$headEulerAngleY");
          if (headEulerAngleY <= DetectionThreshold.LEFT_HEAD_FACING_THRESHOLD) {
            LivenessEventProvider.INSTANCE.post(event);
          }

break;
//                Motion.Up -> {
//                    if (upData(face)) {
//                        LivenessEventProvider.post(event)
//                    }
//                }
//
//                Motion.Down -> {
//                    if (upData(face)) {
//                        LivenessEventProvider.post(event)
//                    }
//                }
      }
    }
    else {
      if (!isEventSent) {
        event.setType(LivenessEventProvider.LivenessEvent.Type.NotMatch);
        LivenessEventProvider.INSTANCE.post(event);
        isEventSent = true;
      }
    }
  }

  private void  processDefaultPosition(Face face) {
    float headEulerAngleY = face.getHeadEulerAngleY();
      LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
    event.setType(LivenessEventProvider.LivenessEvent.Type.Default);

    if (headEulerAngleY < 5 && headEulerAngleY > -5 && !isEventSent) {
      isEventSent = true;
      LivenessEventProvider.INSTANCE.post(event);
    }
  }

  private void  processOpenMouth(Face face) {
    // https://stackoverflow.com/questions/42107466/android-mobile-vision-api-detect-mouth-is-open/43116414
    if (face == null) {
      return;
    }

    if (this.faceId == -1 || face.getTrackingId() == faceId) {
      if (face.getLandmark(FaceLandmark.MOUTH_BOTTOM) != null
              && face.getLandmark(FaceLandmark.MOUTH_LEFT) != null
              && face.getLandmark(FaceLandmark.MOUTH_RIGHT) != null) {
        float cBottomMouthY = face.getLandmark(FaceLandmark.MOUTH_BOTTOM).getPosition().y;
          float cLeftMouthY = face.getLandmark(FaceLandmark.MOUTH_LEFT).getPosition().y;
          float cRightMouthY = face.getLandmark(FaceLandmark.MOUTH_RIGHT).getPosition().y;
          float centerPointY = (cLeftMouthY + cRightMouthY) / 2 - 15;

          float differenceY = centerPointY - cBottomMouthY;

        Log.i(TAG, "draw: difference Y >> $differenceY");

        if (differenceY < DetectionThreshold.OPEN_MOUTH_THRESHOLD && !isMouthOpen) {
          LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
          event.setType(LivenessEventProvider.LivenessEvent.Type.MouthOpen);

          isMouthOpen = true;
          LivenessEventProvider.INSTANCE.post(event);
        }

        if (differenceY >= DetectionThreshold.CLOSED_MOUTH_THRESHOLD) {
          isMouthOpen = false;
        }
      }
    }
    else {
      if (!isEventSent) {
        LivenessEventProvider.LivenessEvent event = new LivenessEventProvider.LivenessEvent();
        event.setType(LivenessEventProvider.LivenessEvent.Type.NotMatch);
        LivenessEventProvider.INSTANCE.post(event);
        isEventSent = true;
      }
    }
  }

  void  setChallengeOrder(ArrayList<Integer> challengeOrder ) {
    this.challengeOrder = challengeOrder;
    this.verificationStep = challengeOrder.get(0);
  }

  private boolean upData(Face face) {
//    if (face != null) {
//      val earPosition = getEarPosition(face)
//      val eyePosition = getEyePosition(face)
//      Log.e(TAG, "Ear position: $earPosition")
//      Log.e(TAG, "Eye position : $eyePosition")
//
//      return if (eyePosition.toDouble() == 0.0 || earPosition.toDouble() == 0.0) {
//        false
//      } else {
//        eyePosition - earPosition > 20
//      }
//    } else {
      return false;
//    }
  }


  private float getEarPosition(Face face) {
//    val leftEar: FirebaseVisionFaceLandmark? = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
//    val rightEar: FirebaseVisionFaceLandmark? = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EAR)
//
//    return if (leftEar == null && rightEar == null) {
//      0f
//    } else if (leftEar != null && rightEar == null) {
//      leftEar.position.y
//    } else if (leftEar == null && rightEar != null) {
//      rightEar.position.y
//    } else {
//      rightEar!!.position.y + leftEar!!.position.y / 2
//    }
    return 0f;
  }

  private float getEyePosition(Face face) {
//    val leftEye: FirebaseVisionFaceLandmark? = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
//    val rightEye: FirebaseVisionFaceLandmark? = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
//
//    return if (leftEye == null && rightEye == null) {
//      0f
//    } else if (leftEye != null && rightEye == null) {
//      leftEye.position.y
//    } else if (leftEye == null && rightEye != null) {
//      rightEye.position.y
//    } else {
//      rightEye!!.position.y + leftEye!!.position.y / 2
//    }
//  }
    return 0f;
  }
}

