package com.gnomeshift.entities

import com.gnomeshift.schemas.Roles
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class RoleEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by Roles.name

    companion object : IntEntityClass<RoleEntity>(Roles)

    fun toUserRole(): UserRole = UserRole.valueOf(name)
}