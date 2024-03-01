package com.chatcallapp.chatcallfirebase.utils

import java.util.regex.Matcher
import java.util.regex.Pattern


object ValidateUtil {
    private val EMAIL_PATTERN =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    private val mEmailPatten = Pattern.compile(EMAIL_PATTERN)

    fun isValidEmail(email: String?): Boolean {
        if (email == null)
            return false
        val matcher: Matcher = mEmailPatten.matcher(email)
        return matcher.matches()
    }
}