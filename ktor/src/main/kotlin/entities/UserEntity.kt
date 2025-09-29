package com.gnomeshift.entities

import com.gnomeshift.schemas.UserRoles
import com.gnomeshift.schemas.UserService
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by UserService.Users.name
    var age by UserService.Users.age
    var username by UserService.Users.username
    var password by UserService.Users.password
    val roles by RoleEntity via UserRoles

    companion object : IntEntityClass<UserEntity>(UserService.Users)

    fun toDto(): User {
        val userRoles = roles.map { it.toUserRole() }
        return User(id.value, name, age, username, password, userRoles)
    }
}
