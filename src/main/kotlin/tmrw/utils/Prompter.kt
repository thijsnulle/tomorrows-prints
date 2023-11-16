package tmrw.utils

import com.aallam.openai.api.LegacyOpenAI
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.runBlocking

data class Example(val input: String, val output: String)

abstract class Prompter<Response>(
    val prompt: String,
    val examples: List<Example>
) {
    private val openAI = OpenAI(
        token = dotenv().get("OPENAI_KEY"),
        logging = LoggingConfig(logLevel = LogLevel.None)
    )

    @OptIn(LegacyOpenAI::class)
    fun ask(input: String): Response = runBlocking {
        val request = CompletionRequest(
            model = ModelId("gpt-3.5-turbo-instruct"),
            prompt = formatPrompt(input),
            maxTokens = 128,
        )

        flow {
            emit(process(openAI.completion(request).choices.map { it.text }.first()))
        }.retry(3).first()
    }

    private fun formatPrompt(input: String): String {
        return """
$prompt
            
${examples.joinToString("\n##\n") {
"""
    input: ${it.input}
    output: ${it.output}
""".trimIndent()}
}
##
input: $input
output: 
""".trimIndent()
    }

    abstract fun process(output: String): Response
}
