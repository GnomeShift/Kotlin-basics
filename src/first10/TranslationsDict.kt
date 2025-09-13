package first10

import kotlin.random.Random

fun findTranslation(dict: Map<String, String>, word: String): String {
    if (dict.containsKey(word)) {
        return dict[word]!!
    }
    return "Ничего не найдено"
}

fun main() {
    val dict = mapOf(
        "system" to "система",
        "keyboard" to "клавиатура",
        "network" to "сеть",
        "kotlin" to "котлин",
        "java" to "джава"
    )
    val word:String = dict.entries.elementAt(Random.nextInt(dict.size)).key

    println("\"$word\" переводится на русский как \"${findTranslation(dict, word)}\"")
}