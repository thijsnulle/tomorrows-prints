package utility.prompt

import com.aallam.openai.api.LegacyOpenAI
import com.aallam.openai.api.completion.CompletionRequest
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI

data class Example(val input: String, val output: String)

abstract class AbstractOpenAIPromptHandler<Response>(
    val prompt: String,
    val examples: List<Example>
): PromptHandler<Response> {
    private val openAI = OpenAI(
        token="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",
        logging = LoggingConfig(logLevel = LogLevel.None)
    )

    @OptIn(LegacyOpenAI::class)
    override suspend fun ask(input: String): Response {
        val request = CompletionRequest(
            model = ModelId("gpt-3.5-turbo-instruct"),
            prompt = formatPrompt(input)
        )

        return process(
            openAI.completion(request).choices.map { it.text }.first()
        )
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
