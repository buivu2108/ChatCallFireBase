package com.chatcallapp.chatcallfirebase.activity

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.databinding.ActivityProfilesBinding
import com.chatcallapp.chatcallfirebase.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class ProfilesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfilesBinding

    private var firebaseUser: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null

    private var filePath: Uri? = null

    private val PICK_IMAGE_REQUEST: Int = 1000

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            databaseReference =
                FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser!!.uid)
        }
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
    }

    private fun initEvent() {
        databaseReference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.etUserName.setText(user!!.userName)

                if (user.profileImage == "") {
                    binding.userImage.setImageResource(R.drawable.profile_image)
                } else {
                    Glide.with(this@ProfilesActivity).load(user.profileImage)
                        .into(binding.userImage)
                }
            }
        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.userImage.setOnClickListener {
            chooseImage()
        }

        binding.btnSave.setOnClickListener {
            uploadImage()
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val ref: StorageReference = storageRef.child("images/${filePath!!.lastPathSegment}")
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["userName"] = binding.etUserName.text.toString()
                    hashMap["profileImage"] = filePath.toString()
                    databaseReference?.updateChildren(hashMap as Map<String, Any>)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(applicationContext, "Upload Success", Toast.LENGTH_SHORT).show()
                    binding.btnSave.visibility = View.GONE
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        applicationContext,
                        "Upload Failed" + it.message,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) {
            filePath = data!!.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.userImage.setImageBitmap(bitmap)
                binding.btnSave.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}