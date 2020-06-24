package com.example.pop3server.data

import android.content.SharedPreferences
import com.example.pop3server.data.model.Mail

class MailHolder {

    companion object {
        var mail: Mail? = null

        var loginUser: String = ""

        lateinit var sp: SharedPreferences
    }

}