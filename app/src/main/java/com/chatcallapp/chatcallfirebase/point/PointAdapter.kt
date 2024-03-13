package com.chatcallapp.chatcallfirebase.point

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chatcallapp.chatcallfirebase.R
import com.chatcallapp.chatcallfirebase.model.ClientPackage

class PointAdapter(
    private var listPoint: MutableList<ClientPackage>, private val positionList: (Int) -> Unit
) : RecyclerView.Adapter<PointAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.layout_item_point, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtPoint.text = "${listPoint[position].point}"
        holder.txtPrice.text = listPoint[position].serverPrice
        holder.itemView.setOnClickListener { positionList.invoke(position) }
    }

    override fun getItemCount(): Int = listPoint.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        var txtPoint: TextView = itemView.findViewById(R.id.txtPoint)
    }
}