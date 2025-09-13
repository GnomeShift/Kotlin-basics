package first10

import kotlin.random.Random

fun numToWeekDay(num: Int): String {
    return when (num) {
        1 -> "Понедельник"
        2 -> "Вторник"
        3 -> "Среда"
        4 -> "Четверг"
        5 -> "Пятница"
        6 -> "Суббота"
        7 -> "Воскресенье"
        else -> "Неизвестно"
    }
}

fun main() {
    val num = Random.nextInt(9)

    println("День недели $num - ${numToWeekDay(num)}")
}