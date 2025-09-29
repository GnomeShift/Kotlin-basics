package com.gnomeshift.schemas

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object Roles : IntIdTable() {
    val name = varchar("name", 255).uniqueIndex()
}

object UserRoles : Table(name = "user_roles") {
    val roleId = reference("role_id", Roles.id, ReferenceOption.CASCADE)
    val userId = reference("user_id", UserService.Users.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userId, roleId)
}