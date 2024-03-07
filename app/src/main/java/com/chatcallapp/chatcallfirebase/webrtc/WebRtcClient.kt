package com.chatcallapp.chatcallfirebase.webrtc

import android.content.Context
import android.media.AudioManager
import com.chatcallapp.chatcallfirebase.utils.DataModel
import com.chatcallapp.chatcallfirebase.utils.DataModelType
import com.google.gson.Gson
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcClient(
    private val context: Context,
    observer: PeerConnectionObserver,
    private val username: String
) {

    private val gson: Gson = Gson()
    private val eglBaseContext: EglBase.Context = EglBase.create().eglBaseContext
    private var peerConnection: PeerConnection
    private var peerConnectionFactory: PeerConnectionFactory
    private val iceServer: MutableList<PeerConnection.IceServer> = arrayListOf()
    private lateinit var videoCapturer: CameraVideoCapturer
    private var localVideoSource: VideoSource
    private var localAudioSource: AudioSource
    private lateinit var localVideoTrack: VideoTrack
    private lateinit var localAudioTrack: AudioTrack
    private lateinit var localStream: MediaStream
    private val localTrackId = "local_track"
    private val localStreamId = "local_stream"
    private val mediaConstraints: MediaConstraints = MediaConstraints()
    private lateinit var listener: Listener

    init {
        initPeerConnectionFactory()
        peerConnectionFactory = createPeerConnectionFactory()
        iceServer.add(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        peerConnection = createPeerConnection(observer)
        localVideoSource = peerConnectionFactory.createVideoSource(false)
        localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private fun initPeerConnectionFactory() {
        val options: PeerConnectionFactory.InitializationOptions =
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .setEnableInternalTracer(true).createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.Options()
        options.disableEncryption = false
        options.disableNetworkMonitor = false
        return PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setOptions(options)
            .createPeerConnectionFactory()
    }

    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection {
        return peerConnectionFactory.createPeerConnection(iceServer, observer)!!
    }

    private fun initSurfaceViewRendered(viewRenderer: SurfaceViewRenderer) {
        viewRenderer.setEnableHardwareScaler(true)
        viewRenderer.setMirror(true)
        viewRenderer.init(eglBaseContext, null)
    }

    fun initLocalSurfaceView(view: SurfaceViewRenderer) {
        initSurfaceViewRendered(view)
        startLocalVideoStreaming(view)
    }

    private fun startLocalVideoStreaming(view: SurfaceViewRenderer) {
        val helpers = SurfaceTextureHelper.create(
            Thread.currentThread().name,
            eglBaseContext
        )

        videoCapturer = getVideoCapture()
        videoCapturer.initialize(helpers, context, localVideoSource.capturerObserver)
        videoCapturer.startCapture(480, 360, 15)

        localVideoTrack = peerConnectionFactory.createVideoTrack(
            localTrackId + "_video", localVideoSource
        )
        localVideoTrack.addSink(view)

        localAudioTrack = peerConnectionFactory.createAudioTrack(
            localTrackId + "_audio", localAudioSource
        )

        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)

        setAudioOutputToSpeaker(view.context)

        peerConnection.addStream(localStream)
    }

    private fun setAudioOutputToSpeaker(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Set audio mode to MODE_IN_COMMUNICATION for WebRTC audio
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        // Set audio routing to speaker
        audioManager.isSpeakerphoneOn = true
    }

    fun initRemoteSurfaceView(view: SurfaceViewRenderer) {
        initSurfaceViewRendered(view)
    }

    private fun getVideoCapture(): CameraVideoCapturer {
        val enumerator = Camera2Enumerator(context)
        val deviceNames = enumerator.deviceNames
        deviceNames.forEach { device ->
            if (enumerator.isFrontFacing(device)) {
                return enumerator.createCapturer(device, null)
            }
        }
        throw IllegalStateException("Front facing camera not found")
    }

    fun call(target: String, userTargetName: String) {
        try {
            peerConnection.createOffer(object : SDPObserver() {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    super.onCreateSuccess(p0)
                    peerConnection.setLocalDescription(
                        object : SDPObserver() {
                            override fun onSetSuccess() {
                                super.onSetSuccess()

                                // transfer sdp to other peer
                                listener.onTransferDataToOtherPeer(
                                    DataModel(
                                        target,
                                        username,
                                        p0?.description,
                                        DataModelType.Offer,
                                        userTargetName
                                    )
                                )
                            }
                        }, p0
                    )
                }
            }, mediaConstraints)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun answer(target: String) {
        try {
            peerConnection.createAnswer(object : SDPObserver() {
                override fun onCreateSuccess(p0: SessionDescription?) {
                    super.onCreateSuccess(p0)
                    peerConnection.setLocalDescription(
                        object : SDPObserver() {
                            override fun onSetSuccess() {
                                super.onSetSuccess()

                                // transfer sdp to other peer
                                listener.onTransferDataToOtherPeer(
                                    DataModel(
                                        target,
                                        username,
                                        p0?.description,
                                        DataModelType.Answer, ""
                                    )
                                )
                            }
                        }, p0
                    )
                }
            }, mediaConstraints)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(SDPObserver(), sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, target: String) {
        addIceCandidate(iceCandidate)
        listener.onTransferDataToOtherPeer(
            DataModel(target, username, gson.toJson(iceCandidate), DataModelType.IceCandidate, "")
        )
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }

    fun toggleVideo(isMuted: Boolean) {
        localVideoTrack.setEnabled(!isMuted)
    }

    fun toggleAudio(isMuted: Boolean) {
        localAudioTrack.setEnabled(!isMuted)
    }

    fun closeConnection() {
        try {
            localVideoTrack.dispose()
//            localAudioTrack.dispose()
            videoCapturer.stopCapture()
            videoCapturer.dispose()
            peerConnection.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onTransferDataToOtherPeer(model: DataModel)
    }
}