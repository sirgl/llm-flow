package sirgl.model.agent

//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.map
//import org.example.OllamaClientAPI
//
//class Ollama(val client: OllamaClientAPI) : Llm {
//    override suspend fun request(messages: List<Message>): Flow<String> {
//        return client.generate(messages.joinToString("") { it.content }).map { it.response }
//    }
//}
//
//class OpenAi(private val token: String) : Llm {
//    override suspend fun request(messages: List<Message>): Flow<String> {
//        TODO("Not yet implemented")
//    }
//}
//
//interface Llm {
//    suspend fun request(messages: List<Message>) : Flow<String>
//}
//
//class Message(
//    val content: String,
//    val role: Role
//)
//
//enum class Role {
//    System,
//    User,
//    Assistant,
//}