package com.example.pop3server.data.parser

import android.util.Base64
import com.example.pop3server.data.MailHolder
import com.example.pop3server.data.model.MailDetail
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import javax.mail.*
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility.decodeText


class MailParser {

    companion object {

        val utf8matcher = Pattern.compile("=\\?(UTF|utf)-8\\?[bB]\\?.+\\?=")
        var session: Session = Session.getInstance(Properties())

        val utf8Prefix = "=?utf-8?B?"

        /// 解析Email链接
        fun parseEmail(resp: String): MailDetail {

            var from: String = ""
            var to: String = ""
            var subject: String = ""
            var date: String = ""
            var body: String = ""
            var inputStream: InputStream = ByteArrayInputStream(resp.toByteArray())
            val message: MimeMessage = MimeMessage(session, inputStream)
            if (message.subject != null) {
                subject = message.subject.toString()
            }
            if (subject.startsWith(utf8Prefix)) {
                // 去除最后的?=
                subject = subject.substring(0, subject.length - 2).removePrefix(utf8Prefix)
                    .replace("\t", "")
                try {
                    subject = String(Base64.decode(subject, Base64.DEFAULT))
                } catch (e: IllegalArgumentException) {
                    subject = message.subject.toString()
                    e.printStackTrace()
                }
            }
            if (message.from != null && message.from.size > 0) {
                from = message.from[0].toString()
                var matcher = utf8matcher.matcher(from)
                while (matcher.find()) {
                    try {
                        val originStr = matcher.group()
                        var replaceStr =
                            originStr.substring(utf8Prefix.length, originStr.length - 2)
                                .removePrefix(utf8Prefix)
                                .removePrefix(utf8Prefix.toLowerCase())
                        replaceStr = String(Base64.decode(replaceStr, Base64.DEFAULT))
                        from = from.replace(originStr, replaceStr)
                        matcher = utf8matcher.matcher(from)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }
            }
            to = MailHolder.loginUser
            if (message.sentDate != null) {
                date = message.sentDate.toLocaleString()
            }
            if (message.content != null) {
                body = message.content.toString()
            }
//            try {
//                val matcher = subjectMatcher.matcher(resp)
//                if (matcher.find()) {
//                    val s = matcher.group(0)
//                    s?.let {
//                        val base64s = it.removePrefix("Subject: =?utf-8?B?")
//                        val subjects = String(Base64.decode(base64s,Base64.DEFAULT))
//                        subject = subjects
//                    }
//                }
//            } catch (e : IllegalArgumentException){
//                Log.e("Parse Error",e.toString())
//            }
            // 判断是否有附件
            return MailDetail(from, to, subject, date, false, body)
        }

        @Throws(MessagingException::class, IOException::class)
        fun isContainAttachment(part: Part): Boolean {
            var flag = false
            if (part.isMimeType("multipart/*")) {
                val multipart: MimeMultipart = part.getContent() as MimeMultipart
                val partCount: Int = multipart.getCount()
                var i = 0
                while (i < partCount) {
                    val bodyPart: BodyPart = multipart.getBodyPart(i)
                    val disp = bodyPart.disposition
                    if (disp != null && (disp.equals(
                            Part.ATTACHMENT,
                            ignoreCase = true
                        ) || disp.equals(
                            Part.INLINE,
                            ignoreCase = true
                        ))
                    ) {
                        flag = true
                    } else if (bodyPart.isMimeType("multipart/*")) {
                        flag = isContainAttachment(bodyPart)
                    } else {
                        val contentType = bodyPart.contentType
                        if (contentType.indexOf("application") != -1) {
                            flag = true
                        }
                        if (contentType.indexOf("name") != -1) {
                            flag = true
                        }
                    }
                    if (flag) break
                    i++
                }
            } else if (part.isMimeType("message/rfc822")) {
                flag = isContainAttachment(part.getContent() as Part)
            }
            return flag
        }
    }


//    @Throws(
//        UnsupportedEncodingException::class,
//        MessagingException::class,
//        FileNotFoundException::class,
//        IOException::class
//    )
//    private fun saveAttachment(
//        part: Part,
//        destDir: String,
//        email: String,
//        sendName: String
//    ) {
//        if (part.isMimeType("multipart/*")) {
//            val multipart = part.content as Multipart // 复杂体邮件
//            // 复杂体邮件包含多个邮件体
//            val partCount = multipart.count
//            val i = 0
//            while (i < partCount) {
//                // 获得复杂体邮件中其中一个邮件体
//                val bodyPart = multipart.getBodyPart(i)
//                // 某一个邮件体也有可能是由多个邮件体组成的复杂体
//                val disp = bodyPart.disposition
//                if (disp != null && (disp.equals(
//                        Part.ATTACHMENT,
//                        ignoreCase = true
//                    ) || disp.equals(Part.INLINE, ignoreCase = true))
//                ) {
//                    val `is` = bodyPart.inputStream
//                    this.saveFile(
//                        `is`,
//                        destDir,
//                        decodeText(bodyPart.fileName),
//                        email,
//                        sendName
//                    )
//                } else if (bodyPart.isMimeType("multipart/*")) {
//                    saveAttachment(bodyPart, destDir, email, sendName)
//                } else {
//                    val contentType = bodyPart.contentType
//                    if (contentType.indexOf("name") != -1 || contentType.indexOf("application") != -1) {
//                        this.saveFile(
//                            bodyPart.inputStream,
//                            destDir,
//                            decodeText(bodyPart.fileName),
//                            email,
//                            sendName
//                        )
//                    }
//                }
//                i
//            }
//        } else if (part.isMimeType("message/rfc822")) {
//            saveAttachment(part.content as Part, destDir, email, sendName)
//        }
//    }


}