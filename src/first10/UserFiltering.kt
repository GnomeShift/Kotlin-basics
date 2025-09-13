package first10

data class User (
    val id:Int,
    var name:String,
    var isActive:Boolean
)

fun main() {
    val users = listOf(
        User(1, "user1", false),
        User(2, "user2", true),
        User(3, "user3", false),
        User(4, "user4", true),
        User(5, "user5", false)
    )

    val active = users.filter { it.isActive }

    println("Active users:")
    active.forEach { println("ID: ${it.id}, Name: ${it.name}") }
}