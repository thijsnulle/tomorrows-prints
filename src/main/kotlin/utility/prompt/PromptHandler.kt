package utility.prompt

fun interface PromptHandler<Response> {
    fun ask(input: String): Response
}
