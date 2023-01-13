package com.katiearose.sobriety.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform