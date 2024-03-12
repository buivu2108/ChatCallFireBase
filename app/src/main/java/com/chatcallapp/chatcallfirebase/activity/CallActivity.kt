package com.chatcallapp.chatcallfirebase.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.databinding.ActivityCallBinding
import com.chatcallapp.chatcallfirebase.repository.MainRepository
import com.chatcallapp.chatcallfirebase.utils.CameraModel
import com.chatcallapp.chatcallfirebase.utils.ErrorCallBack
import com.chatcallapp.chatcallfirebase.utils.NewCameraCallBack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallActivity : AppCompatActivity() {

    private var isBackCamera: Boolean = false
    private var isMutedAudio = false
    private var isMutedVideo = false
    private var userTargetId: String? = null
    private var userTargetName: String? = null
    private var typeCall: Int? = null

    private lateinit var binding: ActivityCallBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userTargetId = intent.getStringExtra("userTargetId")
        userTargetName = intent.getStringExtra("userTargetName")
        typeCall = intent.getIntExtra("typeCall", 0)

        initView()
        initEvent()
    }

    private fun initView() {
        MainRepository.getInstance().initLocalView(binding.svrLocalView)
        MainRepository.getInstance().initRemoteView(binding.svrRemoteView)

        MainRepository.getInstance().subscribeSwitchCameraEvent(object : NewCameraCallBack {
            override fun onCameraSwitch(cameraModel: CameraModel) {
                binding.svrRemoteView.setMirror(cameraModel.isFrontCamera)
            }
        })
    }

    private fun initEvent() {
        binding.btnMic.setOnClickListener {
            isMutedAudio = !isMutedAudio
            binding.btnMic.setImageResource(if (isMutedAudio) R.drawable.baseline_mic_off_24 else R.drawable.baseline_mic_none_24)
            MainRepository.getInstance().toggleAudio(isMutedAudio)
        }
        binding.btnVideo.setOnClickListener {
            isMutedVideo = !isMutedVideo
            binding.btnVideo.setImageResource(if (isMutedVideo) R.drawable.baseline_videocam_off_24 else R.drawable.baseline_videocam_24)
            MainRepository.getInstance().toggleVideo(isMutedVideo)
        }

        binding.btnEndCall.setOnClickListener {
            MainRepository.getInstance().endCall()
            finish()
        }

        binding.btnSwitchCamera.setOnClickListener {
            MainRepository.getInstance().switchCamera()
            binding.svrLocalView.setMirror(isBackCamera)
            isBackCamera = !isBackCamera

            MainRepository.getInstance()
                .sendSwitchCameraEvent(!isBackCamera, object : ErrorCallBack {
                    override fun onError() {
                        Toast.makeText(
                            applicationContext,
                            "Couldn't find the target user!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch  {
            if (typeCall == 1 && !userTargetId.isNullOrEmpty()) {
                withContext(Dispatchers.IO) {
                    Thread.sleep(500)
                }
                MainRepository.getInstance().startCall(userTargetId!!, userTargetName ?: "")
            }
        }
    }
}