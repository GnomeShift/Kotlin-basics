package com.gnomeshift

import com.gnomeshift.db.configureDB
import com.gnomeshift.security.configureAuthentication
import com.gnomeshift.swagger.configureOpenAPI
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    logger.info("Configuring DB...")
    configureDB()

    logger.info("Configuring auth...")
    configureAuthentication()

    logger.info("Configuring routing...")
    configureRouting()

    logger.info("Configuring OpenAPI...")
    configureOpenAPI()
}
