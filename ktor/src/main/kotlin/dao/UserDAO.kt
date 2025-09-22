package com.gnomeshift.dao

import com.gnomeshift.db.db
import com.gnomeshift.dto.UserRequest
import com.gnomeshift.entities.User
import com.gnomeshift.entities.UserEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object UserDAO {
    fun create(request: UserRequest): Result<User> {
        return try {
            transaction(db) {
                val newUserEntity = UserEntity.new {
                    this.name = request.name
                    this.age = request.age
                }
                Result.Success(newUserEntity.toDto())
            }
        }
        catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    fun getById(id: Int): Result<User> {
        return try {
            transaction(db) {
                val userEntity = UserEntity.findById(id)

                if (userEntity != null) {
                    Result.Success(userEntity.toDto())
                }
                else {
                    Result.Error(Exception("User not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun getAll(): Result<List<User>> {
        return try {
            transaction(db) {
                val users = UserEntity.all().toList().map { it.toDto() }
                Result.Success(users)
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun update(id: Int, request: UserRequest): Result<User> {
        return try {
            transaction(db) {
                val userEntity = UserEntity.findById(id)

                if (userEntity != null) {
                    userEntity.name = request.name
                    userEntity.age = request.age
                    Result.Success(userEntity.toDto())
                }
                else {
                    Result.Error(Exception("User not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }

    fun delete(id: Int): Result<Unit> {
        return try {
            transaction(db) {
                val userEntity = UserEntity.findById(id)

                if (userEntity != null) {
                    userEntity.delete()
                    Result.Success(Unit)
                }
                else {
                    Result.Error(Exception("User not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(Exception(e.message))
        }
    }
}
