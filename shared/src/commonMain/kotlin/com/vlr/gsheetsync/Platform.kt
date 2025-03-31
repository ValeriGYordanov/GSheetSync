package com.vlr.gsheetsync

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform