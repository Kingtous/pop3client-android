package com.example.pop3server.data.model

enum class Pop3Commands {
    USER,
    PASS,
    STAT,
    UIDL,
    LIST,
    RETR,
    DELE,
    TOP,
    NOOP,
    QUIT,
}