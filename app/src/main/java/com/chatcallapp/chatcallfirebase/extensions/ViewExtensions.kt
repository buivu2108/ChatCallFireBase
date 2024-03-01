package com.chatcallapp.chatcallfirebase.extensions

import android.view.View


fun View.setOnSingleClickListener(onClickListener: View.OnClickListener?) {
    if (onClickListener != null) {
        setOnClickListener(object : SingleClickListener() {
            override fun onClicked(v: View?) {
                onClickListener.onClick(v)
            }
        })
    } else {
        setOnClickListener(null)
    }
}
