package com.gnomeshift.dao

import com.gnomeshift.entities.User
import com.gnomeshift.entities.UserEntity
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object UserDAO {
    fun create(name: String, age: Int): Result<User> {
        return try {
            transaction {
                val newUserEntity = UserEntity.new {
                    this.name = name
                    this.age = age
                }
                Result.Success(newUserEntity.toDto())
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun getById(id: Int): Result<User> {
        return try {
            transaction {
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
            Result.Error(e)
        }
    }

    fun getAll(): Result<List<User>> {
        return try {
            transaction {
                val users = UserEntity.all().toList().map { it.toDto() }
                Result.Success(users)
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun update(id: Int, newName: String, newAge: Int): Result<User> {
        return try {
            transaction {
                val userEntity = UserEntity.findById(id)

                if (userEntity != null) {
                    userEntity.name = newName
                    userEntity.age = newAge
                    Result.Success(userEntity.toDto())
                }
                else {
                    Result.Error(Exception("User not found"))
                }
            }
        }
        catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun delete(id: Int): Result<Unit> {
        return try {
            transaction {
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
            Result.Error(e)
        }
    }
}
