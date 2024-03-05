package com.chatcallapp.chatcallfirebase.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.model.User
import de.hdodenhof.circleimageview.CircleImageView

typealias OnItemChatClick = (userItem: User) -> Unit
typealias OnItemCallClick = (userItem: User) -> Unit

class UserAdapter(
    private val context: Context,
    private val userList: ArrayList<User>,
    var onItemChatClick: OnItemChatClick,
    var OnItemCallClick: OnItemCallClick,
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.txtUserName.text = user.userName
        Glide.with(context).load(user.profileImage).placeholder(R.drawable.profile_image)
            .into(holder.imgUser)

        holder.ivChatMessage.setOnClickListener {
            onItemChatClick.invoke(user)
        }

        holder.ivVideoCall.setOnClickListener {
            OnItemCallClick.invoke(user)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.tvUserName)
        val imgUser: CircleImageView = view.findViewById(R.id.userImageItem)
        val ivChatMessage: ImageView = view.findViewById(R.id.ivChatMessage)
        val ivVideoCall: ImageView = view.findViewById(R.id.ivVideoCall)
    }
}