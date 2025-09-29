package com.gnomeshift.dao

import com.gnomeshift.db.db
import com.gnomeshift.entities.RoleEntity
import com.gnomeshift.entities.User
import com.gnomeshift.entities.UserEntity
import com.gnomeshift.entities.UserEntity.Companion.find
import com.gnomeshift.schemas.Roles
import com.gnomeshift.schemas.UserRoles
import com.gnomeshift.schemas.Users
import com.gnomeshift.security.RegisterRequest
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object UserDAO {
    fun create(request: RegisterRequest): Result<User> {
        return try {
            transaction(db) {
                if (find { Users.username eq request.username }.firstOrNull() != null) {
                    return@transaction Result.Error(Exception("User '${request.username}' already exists."))
                }

                val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt()) // pragma: allowlist-secret
                val newUserEntity = UserEntity.new {
                    this.name = request.name
                    this.age = request.age
                    this.username = request.username
                    this.password = hashedPassword // pragma: allowlist-secret
                }

                request.roles.forEach { role ->
                    val roleEntity = RoleEntity.find { Roles.name eq role.name }.firstOrNull()
                        ?: throw Exception("Role ${role.name} doesn't exist.")

                    UserRoles.insert {
                        it[UserRoles.userId] = newUserEntity.id
                        it[UserRoles.roleId] = roleEntity.id
                    }
                }
                Result.Success(newUserEntity.toDto())
            }
        }
        catch (e: Exception) {
            throw Exception(e.message, e.cause)
        }
    }

    fun getById(id: Int): Result<User> {
        return try {
            transaction(db) {
                UserEntity.findById(id)?.toDto()?.let { Result.Success(it) }
                    ?: Result.Error(Exception("User with id $id not found."))
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun findByUsername(username: String): Result<User> {
        return try {
            transaction(db) {
                find { Users.username eq username }.firstOrNull()?.toDto()?.let { Result.Success(it) }
                    ?: Result.Error(Exception("User not found"))
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun getAll(): Result<List<User>> {
        return try {
            transaction(db) {
                Result.Success(UserEntity.all().toList().map { it.toDto() })
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun update(id: Int, request: RegisterRequest): Result<User> {
        return try {
            transaction(db) {
                val userEntity = UserEntity.findById(id) ?: return@transaction Result.Error(Exception("User with id $id not found."))
                userEntity.name = request.name
                userEntity.age = request.age

                if (!request.password.isEmpty()) {
                    updatePassword(id, request.password)
                }
                Result.Success(userEntity.toDto())
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun updatePassword(id: Int, newPassword: String): Result<Unit> { // pragma: allowlist-secret
        return try {
            transaction(db) {
                val userEntity = UserEntity.findById(id) ?: return@transaction Result.Error(Exception("User with id $id not found."))
                userEntity.password = BCrypt.hashpw(newPassword, BCrypt.gensalt()) // pragma: allowlist-secret
                Result.Success(Unit)
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun delete(id: Int): Result<Unit> {
        return try {
            transaction(db) {
                val userEntity = UserEntity.findById(id) ?: return@transaction Result.Error(Exception("User with id $id not found."))
                userEntity.delete()

                Result.Success(Unit)
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }
}
