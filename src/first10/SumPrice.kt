package first10

import java.text.DecimalFormat
import kotlin.random.Random

fun main() {
    val list = mutableListOf<Double>()
    val formatter = DecimalFormat("#,##")

    for (i in 1..5) {
        list.add(formatter.format(Random.nextDouble(11.0)).toDouble())
    }

    println("Sum of all elements: ${list.sum()}")
}