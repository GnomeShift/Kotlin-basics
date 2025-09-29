package com.gnomeshift

import com.gnomeshift.dao.ProductDAO
import com.gnomeshift.dao.Result
import com.gnomeshift.dao.UserDAO
import com.gnomeshift.dto.ProductRequest
import com.gnomeshift.entities.Product
import com.gnomeshift.entities.User
import com.gnomeshift.entities.UserRole
import com.gnomeshift.security.JwtResponse
import com.gnomeshift.security.JwtService
import com.gnomeshift.security.LoginRequest
import com.gnomeshift.security.RegisterRequest
import com.gnomeshift.security.UserIdPrincipal
import com.gnomeshift.security.hasRole
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Routing")

suspend fun <T> ApplicationCall.respondResult(result: Result<T>) {
    when (result) {
        is Result.Success -> {
            val status = when (request.httpMethod) {
                HttpMethod.Post -> HttpStatusCode.Created
                HttpMethod.Delete -> HttpStatusCode.NoContent
                else -> HttpStatusCode.OK
            }

            if (result.data is Unit) {
                respond(status)
            }
            else {
                respond(status, result.data!!)
            }
        }
        is Result.Error -> {
            respond(HttpStatusCode.InternalServerError, result.exception)
        }
    }
}

@GenerateOpenApi
fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    @Tag(["All endpoints"])
    routing {
        @KtorDescription(
            summary = "Default endpoint",
            description = "Returns \"Hello World!\""
        )
        @KtorResponds([
            ResponseEntry("200", String::class)
        ])
        get("/") {
            call.respondText("Hello World!")
        }

        @KtorDescription(
            summary = "About",
            description = "Returns app details"
        )
        @KtorResponds([
            ResponseEntry("200", String::class)
        ])
        get("/about") {
            call.respondText("This is a Kotlin app with Ktor!")
        }

        @KtorDescription(
            summary = "Hello",
            description = "Returns greeting with provided name"
        )
        @KtorResponds([
            ResponseEntry("200", String::class),
            ResponseEntry("400", String::class, description = "If name is missing")
        ])
        get("/hello/{name}") {
            try {
                call.respondText("Hello, ${call.parameters["name"]}")
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }

        @KtorDescription(
            summary = "Echo",
            description = "Returns provided text"
        )
        post("/echo") {
            call.respondText(call.receiveText())
        }

        @KtorDescription(
            summary = "Search",
            description = "Returns \"Search:\" with provided text"
        )
        @KtorResponds([
            ResponseEntry("200", String::class)
        ])
        get("/search") {
            call.respondText("Search: ${call.request.queryParameters["query"]}")
        }

        @KtorDescription(
            summary = "Ping",
            description = "Returns pong"
        )
        @KtorResponds([
            ResponseEntry("200", String::class)
        ])
        get("/ping") {
            call.respondText("pong")
        }

        @Tag(["Auth"])
        @KtorDescription(
            summary = "Registration",
            description = "Register new user"
        )
        @KtorResponds([
            ResponseEntry("201", User::class, description = "User registered successfully"),
            ResponseEntry("400", Nothing::class, description = "Invalid request body")
        ])
        post("/register") {
            val request = call.receive<RegisterRequest>()

            when (val result = UserDAO.create(request)) {
                is Result.Success -> call.respond(HttpStatusCode.Created, result.data)
                is Result.Error -> call.respondResult(result)
            }
        }

        @Tag(["Auth"])
        @KtorDescription(
            summary = "Login",
            description = "Authenticate user and returns JWT token"
        )
        @KtorResponds([
            ResponseEntry("200", JwtResponse::class, description = "Successful login"),
            ResponseEntry("400", Nothing::class, description = "Invalid request body"),
            ResponseEntry("401", JwtResponse::class, description = "Invalid credentials")
        ])
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()

            when (val userResult = UserDAO.findByUsername(loginRequest.username)) {
                is Result.Success -> {
                    val user = userResult.data

                    if (BCrypt.checkpw(loginRequest.password, user.password)) {
                        val token = JwtService.generateToken(user.id, user.username, user.roles)
                        call.respond(JwtResponse(token, user))
                    }
                    else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    }
                }
                else -> {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                }
            }
        }
        authenticate("jwt") {
            @Tag(["Users"])
            route("/users") {
                @KtorDescription(
                    summary = "Get all users",
                    description = "Returns all users"
                )
                @KtorResponds([
                    ResponseEntry("200", User::class, description = "Success"),
                    ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                    ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                ])
                get {
                    if (!call.hasRole(UserRole.ADMIN)) {
                        logger.warn("Access denied for ${call.principal<UserIdPrincipal>()?.userId}")
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                        return@get
                    }
                    call.respondResult(UserDAO.getAll())
                }
                route("/{id}") {
                    @KtorDescription(
                        summary = "Get user by id",
                        description = "Returns user by id"
                    )
                    @KtorResponds([
                        ResponseEntry("200", User::class, description = "Success"),
                        ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                        ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                    ])
                    get {
                        val userId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID.")
                        )
                        val idFromToken = call.principal<UserIdPrincipal>()?.userId

                        if (idFromToken != userId && !call.hasRole(UserRole.ADMIN)) {
                            logger.warn("Access denied for user with id $idFromToken.")
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                            return@get
                        }
                        call.respondResult(UserDAO.getById(userId))
                    }

                    @KtorDescription(
                        summary = "Update user by id",
                        description = "Updates user by id"
                    )
                    @KtorResponds([
                        ResponseEntry("200", User::class, description = "Success"),
                        ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                        ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                    ])
                    put {
                        val userId = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID.")
                        )
                        val idFromToken = call.principal<UserIdPrincipal>()?.userId

                        if (idFromToken != userId && !call.hasRole(UserRole.ADMIN)) {
                            logger.warn("Access denied for user with id $idFromToken")
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                            return@put
                        }

                        val updatedUser = call.receive<RegisterRequest>()
                        call.respondResult(UserDAO.update(userId, updatedUser))
                    }

                    @KtorDescription(
                        summary = "Delete user by id",
                        description = "Deletes user by id"
                    )
                    @KtorResponds([
                        ResponseEntry("204", Nothing::class, description = "Success"),
                        ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                        ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                    ])
                    delete {
                        val userId = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID.")
                        )
                        val idFromToken = call.principal<UserIdPrincipal>()?.userId

                        if (idFromToken != userId && !call.hasRole(UserRole.ADMIN)) {
                            logger.warn("Access denied for user with id $idFromToken.")
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                            return@delete
                        }
                        call.respondResult(UserDAO.delete(userId))
                    }
                }
            }

            @Tag(["Products"])
            route("/products") {
                @KtorDescription(
                    summary = "Get all products",
                    description = "Returns all products"
                )
                @KtorResponds([
                    ResponseEntry("200", Product::class, description = "Success"),
                    ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                    ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                ])
                get {
                    call.respondResult(ProductDAO.getAll())
                }

                @KtorDescription(
                    summary = "Add new product",
                    description = "Adds new product"
                )
                @KtorResponds([
                    ResponseEntry("201", Product::class, description = "Success"),
                    ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                    ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                ])
                post {
                    if (!call.hasRole(UserRole.ADMIN)) {
                        logger.warn("Access denied for user with id ${call.principal<UserIdPrincipal>()?.userId}")
                        call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                        return@post
                    }

                    val newProduct = call.receive<ProductRequest>()
                    call.respondResult(ProductDAO.create(newProduct))
                }
                route("/{id}") {
                    @KtorDescription(
                        summary = "Get product by id",
                        description = "Returns product by id"
                    )
                    @KtorResponds([
                        ResponseEntry("200", Product::class, description = "Success"),
                        ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                        ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                    ])
                    get {
                        val productId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid product ID.")
                        )
                        call.respondResult(ProductDAO.getById(productId))
                    }

                    @KtorDescription(
                        summary = "Update product by id",
                        description = "Updates product by id"
                    )
                    @KtorResponds([
                        ResponseEntry("200", Product::class, description = "Success"),
                        ResponseEntry("400", Nothing::class, description = "Invalid request body"),
                        ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                    ])
                    put {
                        val productId = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid product ID.")
                        )

                        if (call.hasRole(UserRole.ADMIN)) {
                            logger.warn("Access denied for user with id ${call.principal<UserIdPrincipal>()?.userId}.")
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                            return@put
                        }

                        val updatedProduct = call.receive<ProductRequest>()
                        call.respondResult(ProductDAO.update(productId, updatedProduct))
                    }

                    @KtorDescription(
                        summary = "Delete product by id",
                        description = "Deletes product by id"
                    )
                    @KtorResponds([
                        ResponseEntry("204", Nothing::class, description = "Success"),
                        ResponseEntry("400", Nothing::class, description = "Invalid product id"),
                        ResponseEntry("403", Nothing::class, description = "Insufficient privileges")
                    ])
                    delete {
                        val productId = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid product ID.")
                        )

                        if (!call.hasRole(UserRole.ADMIN)) {
                            logger.warn("Access denied for user with id ${call.principal<UserIdPrincipal>()?.userId}.")
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Access denied."))
                            return@delete
                        }
                        call.respondResult(ProductDAO.delete(productId))
                    }
                }
            }
        }
    }
}
