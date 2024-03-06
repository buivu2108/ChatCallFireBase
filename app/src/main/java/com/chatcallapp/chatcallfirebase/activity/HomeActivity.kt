package com.chatcallapp.chatcallfirebase.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.databinding.ActivityHomeBinding
import com.chatcallapp.chatcallfirebase.fragment.ProfileFragment
import com.chatcallapp.chatcallfirebase.fragment.UserFragment
import com.chatcallapp.chatcallfirebase.repository.MainRepository
import com.chatcallapp.chatcallfirebase.utils.DataModel
import com.chatcallapp.chatcallfirebase.utils.DataModelType
import com.chatcallapp.chatcallfirebase.utils.NewEventCallBack

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initEvent()
    }

    private fun initView() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(
                R.id.fragment_container_view,
                UserFragment.newInstance()
            ).addToBackStack("UserFragment")
        }
    }

    fun goMyProfile() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view, ProfileFragment.newInstance())
        }
    }

    private fun initEvent() {
        MainRepository.getInstance().subscribeForLatestEvent(object : NewEventCallBack {
            override fun onNewEventReceived(model: DataModel) {
                if (model.type == DataModelType.StartCall) {
                    binding.rlIncomingCall.visibility = View.VISIBLE
                    binding.tvIncomingUsername.text = "${model.sender} is calling..."

                    binding.ibtnEndCall.setOnClickListener {
                        binding.rlIncomingCall.visibility = View.GONE
                    }

                    binding.ibtnStartCall.setOnClickListener {
                        val intent = Intent(this@HomeActivity, CallActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        })
    }
}

