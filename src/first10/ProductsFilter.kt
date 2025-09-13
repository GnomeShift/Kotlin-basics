package first10

data class Product2(
    val id: Int,
    var name: String,
    var price: Double
)

fun main() {
    val products = mutableListOf(
        Product2(1, "Яблоко", 100.0),
        Product2(2, "Груша", 200.0)
    )
    val filtered = products.filter{ it.price > 100 }

    filtered.forEach { println("ID: ${it.id}, Name: ${it.name}, Price: ${it.price}") }
}