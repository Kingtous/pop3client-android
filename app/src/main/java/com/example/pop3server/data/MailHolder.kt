package com.example.pop3server.data

import com.example.pop3server.data.model.Mail

class MailHolder {

    companion object {
        var mail: Mail? = null

        var loginUser: String = ""
    }

}