package sirgl.model.agent

import dev.langchain4j.model.openai.OpenAiChatModel


fun main() {
    val apiKey = System.getenv("GROQ_CLOUD_KEY")
    val model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .baseUrl("https://api.groq.com/openai/v1")
        .modelName("llama3-70b-8192")
//        .responseFormat("json-object")
        .build()
    val answer = model.generate("Say 'Hello World' in json without markdown")
    println(answer) // Hello World
}