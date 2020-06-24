package com.example.pop3server.ui.mail_detail

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View.VISIBLE
import android.widget.Toast
import com.example.pop3server.R
import com.example.pop3server.data.MailHolder
import com.example.pop3server.data.parser.MailParser
import com.example.pop3server.ui.base.BaseActivity
import com.kingtous.remote_unlock.FileTransferTool.FileBrowserActivity
import com.sun.mail.util.BASE64DecoderStream
import kotlinx.android.synthetic.main.activity_maildetail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.mail.BodyPart
import javax.mail.internet.MimeMultipart


class MailDetailActivity : BaseActivity() {

    val pattern: Pattern = Pattern.compile("")
    // 邮件附件存放地
    private lateinit var mailLocalPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "邮件详情"
        MailHolder.mail?.let {
            subject.text = it.detail.subject
            sendDate.text = it.detail.date
            sendTo.text = it.detail.to
            author.text = it.detail.from
            // 拼接成app数据地址
            mailLocalPath =
                filesDir.path + File.separator + MailHolder.loginUser + File.separator + Base64.encode(
                    it.detail.from.toByteArray(),
                    Base64.URL_SAFE
                )
            val file = File(mailLocalPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            // 处理邮件内部逻辑
            it.detail.body?.let { body ->
                handleBody(body)
            }
            // 如果有附件，则显示附件按钮
            if (it.detail.containAttachment) {
                enableContainAttachment()
            }
        }
    }

    fun enableContainAttachment() {
        fab_attachment.setOnClickListener {
            val intent = Intent(this, FileBrowserActivity::class.java)
            intent.putExtra("path", mailLocalPath)
            startActivity(intent)
        }
        fab_attachment.visibility = VISIBLE
    }

    override fun loadLayout(): Int {
        return R.layout.activity_maildetail
    }

    fun handleBody(body: Any) {
        wv_detail.settings.javaScriptEnabled = true
        wv_detail.settings.loadWithOverviewMode = true
        wv_detail.settings.useWideViewPort = true
        wv_detail.settings.minimumFontSize = 16
        wv_detail.settings.textZoom = 150
        wv_detail.settings.setSupportZoom(true)
        wv_detail.settings.builtInZoomControls = true
        wv_detail.settings.displayZoomControls = true
        if (body is String) {
            wv_detail.loadDataWithBaseURL(null, body.toString(), "text/html", "utf-8", null)
        } else if (body is MimeMultipart) {
            parseMimeBodyPart(body)
        } else {
            Toast.makeText(this, "邮件类型目前不支持", Toast.LENGTH_SHORT).show()
        }
        Log.d("web", "载入")
    }

    fun parseMimeBodyPart(body: MimeMultipart) {
        val parts = body.count
        for (index in 0 until parts) {
            val bodyPart: BodyPart = body.getBodyPart(index)
            parseBodyPart(bodyPart)
        }
    }

    fun parseBodyPart(bodyPart: BodyPart) {
        if (bodyPart.contentType != null && bodyPart.content is BASE64DecoderStream) {
            // 附件为二进制
            val fileName = MailParser.parseCharset(getFileName(bodyPart))
            downloadCopy(
                (bodyPart.content as BASE64DecoderStream),
                mailLocalPath + File.separator + fileName
            )
        } else if (bodyPart.contentType != null && bodyPart.contentType.startsWith("text/html")) {
            // 找到了超文本
            wv_detail.loadDataWithBaseURL(
                null,
                bodyPart.content.toString(),
                "text/html",
                "utf-8",
                null
            )

        } else if (bodyPart.contentType != null && bodyPart.contentType.startsWith("multipart/alternative")) {
            // 为超文本和普通文本共存，则只解析html
            if (bodyPart.content is MimeMultipart) {
                val part = bodyPart.content as MimeMultipart
                for (subIndex in 0 until part.count) {
                    val ctype = part.getBodyPart(subIndex).contentType
                    if (ctype != null && ctype.startsWith("text/html")) {
                        // 找到了超文本
                        wv_detail.loadDataWithBaseURL(
                            null,
                            part.getBodyPart(subIndex).content.toString(),
                            "text/html",
                            "utf-8",
                            null
                        )
                        break
                    }
                }
            }
        } else if (bodyPart.contentType != null && bodyPart.contentType.startsWith("multipart/related")) {
            val content = bodyPart.content as MimeMultipart
            for (subIndex in 0 until content.count) {
                parseBodyPart(content.getBodyPart(subIndex))
            }
        }
    }

    val namePrefix = "name=\""
    var df: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINESE)
    fun getFileName(bodyPart: BodyPart): String {
        bodyPart.contentType?.let {
            val index = it.indexOf(namePrefix) + namePrefix.length
            if (it[it.length - 1] == '"') {
                return@getFileName it.substring(index, it.length - 1)
            } else {
                return@getFileName it.substring(index, it.length)
            }
        }
        return "${MailHolder.loginUser}-${df.format(Date())}"
    }

    fun downloadCopy(stream: BASE64DecoderStream, path: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                enableContainAttachment()
            }

            val file = File(path)
            if (file.exists()) {
                return@launch
            }
            try {
                val fileStream = FileOutputStream(path)
                val buff = ByteArray(4096)
                while (stream.available() > 0) {
                    val ch = stream.read(buff, 0, 4096)
                    if (ch == -1) {
                        break
                    } else {
                        fileStream.write(buff, 0, ch)
                    }
                }
                stream.close()
                fileStream.close()
            } catch (e: FileNotFoundException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MailDetailActivity, "邮件数据存在异常", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}