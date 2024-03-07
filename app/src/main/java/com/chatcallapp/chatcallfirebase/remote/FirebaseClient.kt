package com.chatcallapp.chatcallfirebase.remote

import com.chatcallapp.chatcallfirebase.utils.CameraModel
import com.chatcallapp.chatcallfirebase.utils.DataModel
import com.chatcallapp.chatcallfirebase.utils.ErrorCallBack
import com.chatcallapp.chatcallfirebase.utils.NewCameraCallBack
import com.chatcallapp.chatcallfirebase.utils.NewEventCallBack
import com.chatcallapp.chatcallfirebase.utils.SuccessCallBack
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class FirebaseClient {

    companion object {
        const val LATEST_EVENT_FIELD_NAME = "latest_event"
        const val CAMERA_EVENT_FIELD_NAME = "camera_event"
    }

    private val gson: Gson = Gson()
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val dbRefUser: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
    private lateinit var currentUserId: String

    fun signUp(hashMap: HashMap<String, String>, callBack: SuccessCallBack) {
        currentUserId = hashMap["userId"].toString()
        dbRefUser.child(currentUserId).setValue(hashMap).addOnCompleteListener {
            callBack.onSuccess()
        }
    }

    fun login(currentUserId: String) {
        this.currentUserId = currentUserId
    }

    fun sendMessageToOtherUser(dataModel: DataModel, errorCallBack: ErrorCallBack) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child("Users").child(dataModel.target).exists()) {
                    dbRef.child(dataModel.target).child(LATEST_EVENT_FIELD_NAME)
                        .setValue(gson.toJson(dataModel))
                } else {
                    errorCallBack.onError()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                errorCallBack.onError()
            }

        })
    }

    fun observeIncomingLatestEvent(newEventCallBack: NewEventCallBack) {
        dbRef.child(currentUserId).child(LATEST_EVENT_FIELD_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val dataModel =
                            gson.fromJson(snapshot.value.toString(), DataModel::class.java)
                        dataModel?.let {
                            newEventCallBack.onNewEventReceived(dataModel)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun observeCameraSwitchEvent(newEventCallBack: NewCameraCallBack) {
        dbRef.child(currentUserId).child(CAMERA_EVENT_FIELD_NAME)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val cameraModel =
                            gson.fromJson(snapshot.value.toString(), CameraModel::class.java)
                        cameraModel?.let {
                            newEventCallBack.onCameraSwitch(cameraModel)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun sendCameraMessageToOtherUser(cameraModel: CameraModel, errorCallBack: ErrorCallBack) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(cameraModel.target).exists()) {
                    dbRef.child(cameraModel.target).child(CAMERA_EVENT_FIELD_NAME)
                        .setValue(gson.toJson(cameraModel))
                } else {
                    errorCallBack.onError()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                errorCallBack.onError()
            }

        })
    }
}