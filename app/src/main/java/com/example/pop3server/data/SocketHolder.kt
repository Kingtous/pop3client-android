package com.example.pop3server.data

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.charset.StandardCharsets

/// Socket Hold类，用于保存已连接的Socket
class SocketHolder {
    companion object {
        @Volatile
        var writer: BufferedOutputStream? = null
        @Volatile
        var reader: BufferedInputStream? = null
        @Volatile
        var socket: Socket? = null
        val buff = ByteArray(1024)

        fun createIOStream(): Int {
            socket?.let {
                try {
                    it.getInputStream()?.let { iS ->
                        reader = BufferedInputStream(iS)
                    }
                    it.getOutputStream()?.let { oS ->
                        writer = BufferedOutputStream(oS)
                    }
                    if (reader != null && writer != null) {
                        return@createIOStream 0
                    } else {
                        return@createIOStream -1
                    }
                } catch (e: IOException) {
                    return -2;
                }
            }
            return -3
        }

        fun send(data: String): Int {
            writer?.let {
                try {
                    // 追加 \r\n为标识符
                    val dataAppend = data + "\r\n"
                    it.write(dataAppend.toByteArray(StandardCharsets.UTF_8))
                    it.flush()
                    return@send 0
                } catch (e: IOException) {
                    return@send -1
                }
            }
            return -2
        }

        fun receive(): String {
            reader?.let {
                var ch = it.read(buff, 0, 1024)
                val byteArrayOutputStream = ByteArrayOutputStream()
                while (ch != -1) {
                    byteArrayOutputStream.write(buff, 0, ch)
                    if (it.available() > 0 || buff[ch - 1] != '\n'.toByte()) {
                        ch = it.read(buff, 0, 1024)
                    } else {
                        break
                    }
                }
                return@receive String(byteArrayOutputStream.toByteArray())
            }
            return "-ERR"
        }

        // 如果是邮件，最后末尾应该有.\r\n
        fun receiveEmail(): String {
            reader?.let {
                var ch = it.read(buff, 0, 1024)
                val byteArrayOutputStream = ByteArrayOutputStream()
                while (ch != -1) {
                    if (it.available() > 0 || !(ch >= 3 &&buff[ch - 1] == '\n'.toByte() && buff[ch - 2] == '\r'.toByte()
                                && buff[ch - 3] == '.'.toByte())) {
                        // 此时有数据缓冲，或者末尾不是.\r\n，则继续接收
                        byteArrayOutputStream.write(buff, 0, ch)
                        ch = it.read(buff, 0, 1024)
                        continue
                    }
                    if (it.available() == 0 && (ch >= 3 && buff[ch - 1] == '\n'.toByte() && buff[ch - 2] == '\r'.toByte()
                                && buff[ch - 3] == '.'.toByte())
                    ) {
                        // 如果此时无数据发送至手机端，且结尾为.\r\n，则接受完毕，break
                        byteArrayOutputStream.write(buff, 0, ch - 3)
                        break
                    }
                }
                var mailRaw = String(byteArrayOutputStream.toByteArray())
                mailRaw = mailRaw.substring(mailRaw.indexOf("\r\n") + 2)

                return@receiveEmail mailRaw
            }
            return "-ERR"
        }
    }


}