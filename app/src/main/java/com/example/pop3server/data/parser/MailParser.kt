package com.example.pop3server.data.parser

import android.util.Base64
import com.example.pop3server.data.MailHolder
import com.example.pop3server.data.model.MailDetail
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern
import javax.mail.Session
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


class MailParser {

    companion object {

        val utf8matcher = Pattern.compile("=\\?(UTF|utf)-8\\?[bB]\\?.+\\?=")
        val gb18030matcher = Pattern.compile("=\\?(gb|GB)18030\\?[bB]\\?.+\\?=")
        val ISO88591matcher = Pattern.compile("=\\?(ISO|iso)-8859-1\\?[bB]\\?.+\\?=")
        var session: Session = Session.getInstance(Properties())

        val utf8Prefix = "=?utf-8?B?"
        val gb18030Prefix = "=?gb18030?B?"
        val ISO88591Prefix = "=?ISO-8859-1?B?"

        /// 解析Email链接
        fun parseEmail(resp: String): MailDetail {

            var from: String = ""
            var to: String = ""
            var subject: String = ""
            var date: String = ""
            var body: Any = ""
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

                matcher = gb18030matcher.matcher(from)
                while (matcher.find()) {
                    try {
                        val originStr = matcher.group()
                        var replaceStr =
                            originStr.substring(gb18030Prefix.length, originStr.length - 2)
                                .removePrefix(gb18030Prefix)
                                .removePrefix(gb18030Prefix.toLowerCase())
                        replaceStr = String(Base64.decode(replaceStr, Base64.DEFAULT))
                        from = from.replace(originStr, replaceStr)
                        matcher = gb18030matcher.matcher(from)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }

                matcher = ISO88591matcher.matcher(from)
                while (matcher.find()) {
                    try {
                        val originStr = matcher.group()
                        var replaceStr =
                            originStr.substring(ISO88591Prefix.length, originStr.length - 2)
                                .removePrefix(ISO88591Prefix)
                                .removePrefix(ISO88591Prefix.toLowerCase())
                        replaceStr = String(Base64.decode(replaceStr, Base64.DEFAULT))
                        from = from.replace(originStr, replaceStr)
                        matcher = ISO88591matcher.matcher(from)
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
                body = message.content
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
            var isContainAttachment = false
            if (body is MimeMultipart) {
                if (body.contentType != null && body.contentType.startsWith("multipart/mixed")) {
                    isContainAttachment = true
                }
            }
            return MailDetail(from, to, subject, date, isContainAttachment, body)
        }

        public val MIME_MapTable = arrayOf(
            //{后缀名，MIME类型}
            arrayOf("3gp", "video/3gpp"),
            arrayOf("apk", "application/vnd.android.package-archive"),
            arrayOf("asf", "video/x-ms-asf"),
            arrayOf("avi", "video/x-msvideo"),
            arrayOf("bin", "application/octet-stream"),
            arrayOf("bmp", "image/bmp"),
            arrayOf("c", "text/plain"),
            arrayOf("class", "application/octet-stream"),
            arrayOf("conf", "text/plain"),
            arrayOf("cpp", "text/plain"),
            arrayOf("doc", "application/msword"),
            arrayOf(
                "docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ),
            arrayOf("xls", "application/vnd.ms-excel"),
            arrayOf("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            arrayOf("exe", "application/octet-stream"),
            arrayOf("gif", "image/gif"),
            arrayOf("gtar", "application/x-gtar"),
            arrayOf("gz", "application/x-gzip"),
            arrayOf("h", "text/plain"),
            arrayOf("htm", "text/html"),
            arrayOf("html", "text/html"),
            arrayOf("jar", "application/java-archive"),
            arrayOf("java", "text/plain"),
            arrayOf("jpeg", "image/jpeg"),
            arrayOf("jpg", "image/jpeg"),
            arrayOf("js", "application/x-javascript"),
            arrayOf("log", "text/plain"),
            arrayOf("m3u", "audio/x-mpegurl"),
            arrayOf("m4a", "audio/mp4a-latm"),
            arrayOf("m4b", "audio/mp4a-latm"),
            arrayOf("m4p", "audio/mp4a-latm"),
            arrayOf("m4u", "video/vnd.mpegurl"),
            arrayOf("m4v", "video/x-m4v"),
            arrayOf("mov", "video/quicktime"),
            arrayOf("mp2", "audio/x-mpeg"),
            arrayOf("mp3", "audio/x-mpeg"),
            arrayOf("mp4", "video/mp4"),
            arrayOf("mpc", "application/vnd.mpohun.certificate"),
            arrayOf("mpe", "video/mpeg"),
            arrayOf("mpeg", "video/mpeg"),
            arrayOf("mpg", "video/mpeg"),
            arrayOf("mpg4", "video/mp4"),
            arrayOf("mpga", "audio/mpeg"),
            arrayOf("msg", "application/vnd.ms-outlook"),
            arrayOf("ogg", "audio/ogg"),
            arrayOf("pdf", "application/pdf"),
            arrayOf("png", "image/png"),
            arrayOf("pps", "application/vnd.ms-powerpoint"),
            arrayOf("ppt", "application/vnd.ms-powerpoint"),
            arrayOf(
                "pptx",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            ),
            arrayOf("prop", "text/plain"),
            arrayOf("rc", "text/plain"),
            arrayOf("rmvb", "audio/x-pn-realaudio"),
            arrayOf("rtf", "application/rtf"),
            arrayOf("sh", "text/plain"),
            arrayOf("tar", "application/x-tar"),
            arrayOf("tgz", "application/x-compressed"),
            arrayOf("txt", "text/plain"),
            arrayOf("wav", "audio/x-wav"),
            arrayOf("wma", "audio/x-ms-wma"),
            arrayOf("wmv", "audio/x-ms-wmv"),
            arrayOf("wps", "application/vnd.ms-works"),
            arrayOf("xml", "text/plain"),
            arrayOf("z", "application/x-compress"),
            arrayOf("zip", "application/x-zip-compressed"),
            arrayOf("", "*/*")
        )

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