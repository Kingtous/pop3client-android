package com.example.pop3server.ui.mail_detail

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.pop3server.R
import com.example.pop3server.data.MailHolder
import com.example.pop3server.data.parser.MailParser
import kotlinx.android.synthetic.main.*
import kotlinx.android.synthetic.main.activity_maildetail.*

class MailDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maildetail)
        title = "邮件详情"
        MailHolder.mail?.let {
            subject.text = it.detail.subject
            sendDate.text = it.detail.date
            sendTo.text = it.detail.to
            author.text = it.detail.from
            handleBody(it.detail.body)
        }
    }

    fun handleBody(body: Any) {
        wv_detail.loadData(body.toString(), "text/html", "utf-8")
        Log.d("web", "载入")
    }
}