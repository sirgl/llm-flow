package sirgl.model.agent.execution.prompts

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import sirgl.model.agent.ToolSpec
import sirgl.model.agent.execution.AITask
import sirgl.model.agent.execution.History
import sirgl.model.agent.execution.PlanningExecutionEntry

class DecisionPromptData(
    val task: AITask,
    val backstory: String,
    val tools: List<ToolSpec>,
    val underlyingMembers: List<CrewMemberPromptData>,
    val history: History,
)

fun composeHistory(history: History) : String {
    return buildString {
        for (entry in history.sectionLog.entries) {
            when (entry) {
                is PlanningExecutionEntry -> {
                    appendLine("The following plan was suggested:")
                    for (planLine in entry.plan) {
                        appendLine(planLine)
                    }
                }
            }
        }
    }
}

// TODO it would be better if this thing would be customizable - we would just provide a template and everything got inserted inside
//  this way we would be able to
fun composeDecisionPrompt(data: DecisionPromptData): String {
    val toolsPrompt = composeToolsPrompt(data.tools)
    val delegationPrompt = composeDelegationPrompt(data.underlyingMembers)
    val history = composeHistory(data.history)
    return """
        You are an autonomous AI agent working in a crew.
        Your background: ${data.backstory}.
        You need to solve the following task:
        ${data.task.text}.
        The task solution must be presented like following:
        ${data.task.resultFormat}.
        
        The history of your actions:
        $history
        ---
        You have the following tools at your disposal:
        $toolsPrompt
        $delegationPrompt
        If you think that the task can't be accomplished given the resources you have, you should indicate that it is impossible"
        You have to choose exactly one of the following to make a single step towards solving the task and passing it back to manager:
        1. Use tool. Format: { 
            "choice": "Tool", 
            "data": { 
                "reason": "what is required from the tool, how the result is supposed to be used", 
                "name": "<tool name>", 
                "args": ["<arg1>", "arg2"] 
            } 
        }
        2. Delegate. Format: { 
            "choice": "Delegate", 
            "data": { 
                "reason": "what is required from the tool, how the result is supposed to be used",
                "delegate to": "<crew member name>", 
                "task": "task formulated for the member",
                "response format": "format of response (e.g. metric/summary)"
            } 
        }
        3. Finish. Format: {
            "choice" : "Finish",
            "data": {
                "response" : "<response in the format as manager requested>"
            }
        }
        4. Impossible to solve. Format: {
            "choice" : "Impossible",
            "data": {
                "reason": "<reason>",
                "summary": "summary of attempts to solve it"
            }
         }
         5. Do reasoning (just thinking). Format: {
            "choice": "Reasoning",
            "data": {
                "thinking" : "<thoughts>"
            }
         }
    """.trimIndent()
}

object ExecutionChoiceSerializer : JsonContentPolymorphicSerializer<ExecutionChoice>(
    ExecutionChoice::class
) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out ExecutionChoice> {
        return when (element.jsonObject["choice"]?.jsonPrimitive?.content) {
            ExecutionType.Tool.name -> ExecutionChoice.Tool.serializer()
            ExecutionType.Delegate.name -> ExecutionChoice.Delegate.serializer()
            ExecutionType.Finish.name -> ExecutionChoice.Finish.serializer()
            ExecutionType.Impossible.name -> ExecutionChoice.Impossible.serializer()
            ExecutionType.Reasoning.name -> ExecutionChoice.Reasoning.serializer()
            else -> throw Exception("ERROR: No ExecutionType found. Serialization failed.")
        }
    }
}
enum class ExecutionType {
    Tool,
    Delegate,
    Finish,
    Impossible,
    Reasoning,
}

@kotlinx.serialization.Serializable(with = ExecutionChoiceSerializer::class)
sealed class ExecutionChoice() {
    @kotlinx.serialization.Serializable
    data class Tool(val data: ToolData) : ExecutionChoice()

    @kotlinx.serialization.Serializable
    data class Delegate(val data: DelegateData) : ExecutionChoice()

    @kotlinx.serialization.Serializable
    data class Finish(val data: FinishData) : ExecutionChoice()

    @kotlinx.serialization.Serializable
    data class Impossible(val data: ImpossibleData) : ExecutionChoice()

    @kotlinx.serialization.Serializable
    data class Reasoning(val data: ReasoningData) : ExecutionChoice()
}

@kotlinx.serialization.Serializable
abstract class ExecutionData

@kotlinx.serialization.Serializable
data class ToolData(val reason: String, val name: String, val args: List<String>) : ExecutionData()

@kotlinx.serialization.Serializable
data class DelegateData(val reason: String, val delegateTo: String, val task: String, val responseFormat: String) : ExecutionData()

@kotlinx.serialization.Serializable
data class FinishData(val response: String) : ExecutionData()

@kotlinx.serialization.Serializable
data class ImpossibleData(val reason: String, val summary: String) : ExecutionData()

@kotlinx.serialization.Serializable
data class ReasoningData(val thinking: String) : ExecutionData()

val json = Json { ignoreUnknownKeys = true }

fun main() {
    val jsonString = """{
        "choice": "Tool",
        "data": {
            "reason": "We need to search for a specific symbol in the entire project",
            "name": "exact_search",
            "args": ["symbols", "someSymbol"]
        }
    }"""

    try {
        println(
            json.encodeToString(
                serializer(),
                ExecutionChoice.Tool(ToolData("reason", "name", listOf("foo", "bar")))
            )
        )

        val executionChoice = json.decodeFromString(string = jsonString, deserializer = Json.serializersModule.serializer<ExecutionChoice>())

        when (executionChoice) {
            is ExecutionChoice.Tool -> {
                println("Performing tool operation with name ${executionChoice.data.name}")
            }
            is ExecutionChoice.Delegate -> {
                println("Delegating task to ${executionChoice.data.delegateTo}")
            }
            is ExecutionChoice.Finish -> {
                println("Finishing with response: ${executionChoice.data.response}")
            }
            is ExecutionChoice.Impossible -> {
                println("Task execution is impossible due to reason: ${executionChoice.data.reason}")
            }
            is ExecutionChoice.Reasoning -> {
                println("Engaging in reasoning thought process: ${executionChoice.data.thinking}")
            }
        }

    } catch (e: SerializationException) {
        println("Failed to parse JSON: ${e.message}")
    }
}