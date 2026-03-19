package com.enaven.compism.core

object DefaultCompismLogger : CompismLogger {
    override fun log(level: LogLevel, message: String) {
        println("[Compism][$level] $message")
    }
}

interface CompismLogger {
    fun log(level: LogLevel, message: String)
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}