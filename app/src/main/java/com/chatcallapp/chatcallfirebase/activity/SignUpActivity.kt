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
import com.chatcallapp.chatcallfirebase.utils.ValidateUtil
import com.google.firebase.auth.FirebaseAuth

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
            val userName = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPass = binding.etConfirmPassword.text.toString()

            if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (ValidateUtil.isValidEmail(email)) {
                    if (confirmPass == password) {
                        createAccount(userName, email, password)
                    } else {
                        Toast.makeText(
                            this,
                            "Password and confirm are not matching",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this, "Email invalidate", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter complete information", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogin.setOnSingleClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun createAccount(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")

                    val user = auth.currentUser
                    val userId = user?.uid ?: ""

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap["userId"] = userId
                    hashMap["userName"] = userName
                    hashMap["profileImage"] = ""

                    MainRepository.getInstance().signUp(
                        applicationContext,
                        hashMap,
                        userId = userId,
                        object : SuccessCallBack {
                            override fun onSuccess() {
                                val intent = Intent(this@SignUpActivity, HomeActivity::class.java)
                                startActivity(intent)
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
