package utility.prompt

fun interface Prompter<Response> {
    fun ask(input: String): Response
}
