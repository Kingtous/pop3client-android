package com.example.pop3server.data

import java.io.*
import java.net.Socket
import java.nio.charset.StandardCharsets

/// Socket Hold类，用于保存已连接的Socket
class SocketHolder {
    companion object {
        var writer: BufferedOutputStream? = null
        var reader: BufferedInputStream? = null
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
                    val dataAppend = data + "\n"
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
                    byteArrayOutputStream.write(buff, 0, ch)
                    if (it.available() > 0 || !(ch >= 3 &&buff[ch - 1] == '\n'.toByte() && buff[ch - 2] == '\r'.toByte()
                                && buff[ch - 3] == '.'.toByte())) {
                        ch = it.read(buff, 0, 1024)
                    } else {
                        break
                    }
                }
                return@receiveEmail String(byteArrayOutputStream.toByteArray())
            }
            return "-ERR"
        }
    }


}