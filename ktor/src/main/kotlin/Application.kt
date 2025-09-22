package com.gnomeshift

import com.gnomeshift.db.configureDB
import com.gnomeshift.security.configureAuthentication
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDB()
    configureAuthentication()
    configureRouting()
}
