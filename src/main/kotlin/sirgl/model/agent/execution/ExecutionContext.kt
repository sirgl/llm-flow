package sirgl.model.agent.execution

import com.intellij.openapi.project.Project

class ExecutionContext(
    val project: Project,
    val logger: ExecutionLogger,
    val history: History, // TODO - must be var
    // TODO some kind of LLM? or it should be part of a agent?
val llmSet: LlmSet
) {

}

class LlmSet() {
    fun getGeneric() : Llm {
        TODO()
    }

    fun getSmartest() : Llm {
        TODO()
    }

    fun getLongContext() : Llm {
        TODO()
    }
}

class Llm {
    suspend fun request(text: String) : String {
        TODO()
    }
}