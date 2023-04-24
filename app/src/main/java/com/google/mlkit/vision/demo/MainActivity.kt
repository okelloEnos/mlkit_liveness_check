package com.google.mlkit.vision.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.mlkit.vision.demo.liveness.LivenessApp
import com.google.mlkit.vision.demo.liveness.entity.LivenessItem
import com.google.mlkit.vision.demo.liveness.listener.PrivyCameraLivenessCallBackListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var cameraSide: Boolean = false
    private lateinit var dimensionsTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
dimensionsTextView = findViewById(R.id.dimensions)
        buttonStart.setOnClickListener {

            val livenessApp = LivenessApp.Builder(this)
                .setDebugMode(false)
                .setCameraPart(cameraSide)
                .setMotionInstruction("Look left", "Look right")
                .setSuccessText("Successful! Please look at the camera again to take a photo")
                .setInstructions("Look at the camera and place the face on the green circle")
                .build()

            livenessApp.start(object : PrivyCameraLivenessCallBackListener {

                override fun success(livenessItem: LivenessItem?) {
                    if (livenessItem != null) {
                        test_image.setImageBitmap(livenessItem.imageBitmap)
                        dimensionsTextView.text = livenessItem.dimensionsInfo
                    }
                }

                override fun failed(t: Throwable?) {

                }

            })
        }
    }
}