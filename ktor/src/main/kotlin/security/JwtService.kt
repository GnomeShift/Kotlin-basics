package com.gnomeshift.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.gnomeshift.entities.UserRole
import io.ktor.server.config.ApplicationConfig
import org.slf4j.LoggerFactory
import java.io.File
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

private val logger = LoggerFactory.getLogger("JwtService")

fun decodePemContent(pemContent: String): ByteArray {
    val base64Encoded = pemContent
        .replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "") // pragma: allowlist-secret
        .replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
        .replace("\\s+".toRegex(), "")
    return Base64.getDecoder().decode(base64Encoded)
}

fun loadRSAPrivateKey(path: String): RSAPrivateKey {
    val key = File(path)

    if (!key.exists()) {
        throw Exception("Private key file not found at: $path")
    }

    val pemContent = key.readText()
    val keyBytes = decodePemContent(pemContent)
    val spec = PKCS8EncodedKeySpec(keyBytes)
    val kf = KeyFactory.getInstance("RSA")
    return kf.generatePrivate(spec) as RSAPrivateKey
}

fun loadRSAPublicKey(path: String): RSAPublicKey {
    val key = File(path)

    if (!key.exists()) {
        throw Exception("Public key file not found at: $path")
    }

    val pemContent = key.readText()
    val keyBytes = decodePemContent(pemContent)
    val spec = X509EncodedKeySpec(keyBytes)
    val kf = KeyFactory.getInstance("RSA")
    return kf.generatePublic(spec) as RSAPublicKey
}

object JwtService {
    lateinit var algorithm: Algorithm private set
    private lateinit var jwtAudience: String
    private lateinit var jwtIssuer: String
    private var tokenExpirationMinutes: Long = 0

    fun init(config: ApplicationConfig) {
        try {
            val privateKeyPath = config.property("privateKeyPath").getString()
            val publicKeyPath = config.property("publicKeyPath").getString()
            tokenExpirationMinutes = config.property("tokenExpirationMinutes").getString().toLong()
            jwtAudience = config.property("audience").getString()
            jwtIssuer = config.property("issuer").getString()

            val privateKey = loadRSAPrivateKey(privateKeyPath)
            val publicKey = loadRSAPublicKey(publicKeyPath)

            algorithm = Algorithm.RSA256(publicKey, privateKey)
        }
        catch (e: Exception) {
            logger.error("Failed to initialize JwtService.", e)
            throw Exception("Failed to initialize JwtService.", e)
        }
    }

    fun generateToken(userId: Int, username: String, roles: List<UserRole>): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withSubject(username)
            .withClaim("userId", userId)
            .withClaim("roles", roles.map { it.name })
            .withExpiresAt(Date(System.currentTimeMillis() + tokenExpirationMinutes * 60 * 1000))
            .sign(algorithm)
    }

    //todo убрать за ненадобностью
    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = JWT.require(algorithm)
                .withAudience(jwtAudience)
                .withIssuer(jwtIssuer)
                .build()
            verifier.verify(token)
        }
        catch (e: Exception) {
            println("JWT verification failed: ${e.message}")
            null
        }
    }
}
