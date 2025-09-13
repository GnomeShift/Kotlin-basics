package first10

data class Order(
    val id: Int,
    var product: String,
    var quantity: Int,
    var price: Double
)

fun main() {
    val orders = listOf(
        Order(1, "Яблоко", 1, 10.0),
        Order(2, "Груша", 2, 20.0),
        Order(3, "Апельсин", 3, 30.0),
        Order(4, "Банан", 4, 40.0)
    )

    println("Сумма заказов: ${orders.sumOf { it.quantity * it.price }}")
}