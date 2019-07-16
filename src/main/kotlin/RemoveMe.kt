data class RemoveMe(val language: String)

fun main() {
    val prova = RemoveMe("KOTLIN")
    println("""
        I am using a ${DeleteMe.LANGUAGE} class 
        and a ${prova.language} class together!!!
        😍 🤩 😎
         """.trimIndent())
}