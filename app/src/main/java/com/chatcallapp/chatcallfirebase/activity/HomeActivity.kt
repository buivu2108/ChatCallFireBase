package com.chatcallapp.chatcallfirebase.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.adapter.UserAdapter
import com.chatcallapp.chatcallfirebase.databinding.ActivityHomeBinding
import com.chatcallapp.chatcallfirebase.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var userAdapter: UserAdapter? = null
    var userList = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {
        binding.userRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        getUsersList()
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
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUser = snapshot.getValue(User::class.java)
                if (currentUser!!.profileImage == "") {
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@HomeActivity).load(currentUser.profileImage)
                        .into(binding.imgProfile)
                }

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)

                    if (!user!!.userId.equals(firebase.uid)) {

                        userList.add(user)
                    }
                }

                val userAdapter = UserAdapter(this@HomeActivity, userList, onItemClick = { user ->
                    val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                    intent.putExtra("userId", user.userId)
                    intent.putExtra("userName", user.userName)
                    startActivity(intent)
                })

                binding.userRecyclerView.adapter = userAdapter
            }

        })
    }

    private fun initEvent() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }
}