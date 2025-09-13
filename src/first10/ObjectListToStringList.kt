package first10

data class Product(
    val id: Int,
    var name: String
)

fun main() {
    val products = mutableListOf(
        Product(1, "Яблоко"),
        Product(2, "Груша")
    )

    println(products.map { it.name })
}