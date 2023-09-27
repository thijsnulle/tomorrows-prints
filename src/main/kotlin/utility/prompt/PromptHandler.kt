package utility.prompt

fun interface PromptHandler<Response> {
    suspend fun ask(input: String): Response
}
