package com.chatcallapp.chatcallfirebase.fragment

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.activity.ChatActivity
import com.chatcallapp.chatcallfirebase.activity.HomeActivity
import com.chatcallapp.chatcallfirebase.activity.LoginActivity
import com.chatcallapp.chatcallfirebase.adapter.UserAdapter
import com.chatcallapp.chatcallfirebase.databinding.FragmentUserBinding
import com.chatcallapp.chatcallfirebase.extensions.setOnSingleClickListener
import com.chatcallapp.chatcallfirebase.model.User
import com.chatcallapp.chatcallfirebase.repository.MainRepository
import com.chatcallapp.chatcallfirebase.utils.ErrorCallBack
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.permissionx.guolindev.PermissionX

class UserFragment : Fragment() {
    private lateinit var binding: FragmentUserBinding
    var userList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        getUsersList()
        binding.userRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

    private fun getUsersList() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        val userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")


        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Users")

        //get list User tren data base Fire Base
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireActivity(), error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)
                    if (user != null) {
                        if (user.userId != firebase.uid) {
                            userList.add(user)
                        } else {
                            if (user.profileImage == "") {
                                binding.imgProfile.setImageResource(R.drawable.profile_image)
                            } else {
                                Glide.with(this@UserFragment).load(user.profileImage)
                                    .into(binding.imgProfile)
                            }
                        }
                    }
                }

                val userAdapter =
                    UserAdapter(requireContext(), userList, onItemChatClick = { user ->
                        val intent = Intent(requireActivity(), ChatActivity::class.java)
                        intent.putExtra("userId", user.userId)
                        intent.putExtra("userName", user.userName)
                        startActivity(intent)
                    }, OnItemCallClick = { user ->
                        PermissionX.init(requireActivity())
                            .permissions(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                            .request { allGranted, grantedList, deniedList ->
                                if (allGranted) {
                                    startCall(user.userId,user.userName)
                                }
                            }
                    })

                binding.userRecyclerView.adapter = userAdapter
            }

        })
    }

    private fun startCall(userId: String, userName: String) {
        if (userId.isNotEmpty()) {
            // Start a call request
            MainRepository.getInstance().sendCallRequest(userId,userName, object : ErrorCallBack {
                override fun onError() {
                    Toast.makeText(
                        requireContext(),
                        "Couldn't find the target user!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please enter username!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initEvent() {
        binding.imgBack.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.imgProfile.setOnSingleClickListener {
            if (activity is HomeActivity) {
                (activity as HomeActivity).goMyProfile()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()

    }
}