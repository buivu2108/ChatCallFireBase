package com.chatcallapp.chatcallfirebase.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chatcallapp.chatcallfirebase.databinding.ActivitySignUpBinding
import com.chatcallapp.chatcallfirebase.extensions.setOnSingleClickListener
import com.chatcallapp.chatcallfirebase.repository.MainRepository
import com.chatcallapp.chatcallfirebase.utils.SuccessCallBack
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
    }

    private fun initView() {

    }

    private fun initEvent() {
        binding.btnSignUp.setOnSingleClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")

                    val user = auth.currentUser
                    val userId = user?.uid ?: ""

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val current = LocalDateTime.now().format(formatter)

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["userId"] = userId
                    hashMap["userName"] = "Guest$current"
                    hashMap["profileImage"] = ""

                    MainRepository.getInstance().signUp(
                        applicationContext,
                        hashMap,
                        userId = userId,
                        object : SuccessCallBack {
                            override fun onSuccess() {
                                val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        })
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}
