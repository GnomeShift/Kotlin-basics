package first10

fun nullable(str: String?): Any {
    return(str?.length ?: "0")
}

fun main() {
    var str: String? = "Kotlin"
    println("Длина строки: ${nullable(str)}")
    str = null
    println("Длина строки: ${nullable(str)}")
}