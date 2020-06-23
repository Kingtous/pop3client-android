package com.example.pop3server.data

import com.example.pop3server.data.model.LoggedInUser
import com.example.pop3server.data.model.Pop3Commands
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class MailDetailDataSource {

    fun fetchDetail(id: Int): Result<String> = runBlocking {
        if (SocketHolder.send("${Pop3Commands.RETR} $id") == 0) {
            return@runBlocking Result.Success(SocketHolder.receive())
        } else {
            return@runBlocking Result.Error(IOException("-ERR"))
        }
    }
}

