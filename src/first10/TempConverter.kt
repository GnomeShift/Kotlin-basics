package first10

import java.text.DecimalFormat
import kotlin.random.Random

fun tempConverter(c: Double): Double {
    return (c * 9 / 5) + 32
}

fun main() {
    val formatter = DecimalFormat("#,##")
    val c = formatter.format(Random.nextDouble(101.00)).toDouble()

    println("Celsius temp: $c")
    println("Fahrenheit temp: ${tempConverter(c)}")
}