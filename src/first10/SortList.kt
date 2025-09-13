package first10

fun main() {
    val list = mutableListOf(
        1, 3, 2, 4, 5, 6
    )

    println("По возрастанию: ${list.sorted()}")
    println("По убыванию: ${list.sortedDescending()}")
}