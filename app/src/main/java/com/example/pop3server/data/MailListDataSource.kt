package com.example.pop3server.data

import com.example.pop3server.data.model.LoggedInUser
import com.example.pop3server.data.model.Pop3Commands
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class MailListDataSource {

    fun fetch(server: String, username: String, password: String): Result<LoggedInUser> =
        runBlocking {
            withContext(Dispatchers.IO) {
                try {
                    /*连接服务器*/
                    SocketHolder.socket = Socket(server, 110)
                    if (SocketHolder.createIOStream() == 0) {
                        // server : +OK Welcome to Hmail POP3 server
                        var response = SocketHolder.receive()
                        if (response.startsWith("+OK")) {
                            // client : user <username> <LF>
                            // 发送用户名
                            SocketHolder.send("${Pop3Commands.USER} $username")
                            response = SocketHolder.receive()
                            // server : +OK ...
                            if (response.startsWith("+OK")) {
                                // client : pass password
                                SocketHolder.send("${Pop3Commands.PASS} $password")
                                response = SocketHolder.receive()
                                if (response.startsWith("+OK")){
                                    // 成功登陆
                                    return@withContext Result.Success(LoggedInUser(username))
                                }
                            }
                        }
                        return@withContext Result.Error(IOException("Incorrect"))
                    } else {
                        return@withContext Result.Error(IOException("Error Connect Server"))
                    }
                } catch (e: Throwable) {
                    Result.Error(IOException("Error logging in", e))
                }
            }
        }

    fun logout() {
        // TODO: revoke authentication
    }
}

