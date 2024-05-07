package sirgl.model.agent.execution

import kotlinx.serialization.Serializable
import sirgl.model.agent.ParamSpec
import sirgl.model.agent.ToolSpec

// TODO history already required here :( Previous attempts may fail
class ListPlanPromptData(
    val task: String,
    val resultFormat: String,
    val backstory: String,
    val tools: List<ToolSpec>,
    val underlyingMembers: List<CrewMemberPromptData>
//    val
)

class CrewMemberPromptData(
    val name: String,
    val toolNames: List<String>,
    val shortDescription: String
)

fun composePlanPrompt(data: ListPlanPromptData): String {
    val toolsPrompt = composeToolsPrompt(data.tools)
    val delegationPrompt = composeDelegationPrompt(data.underlyingMembers)
    return """
        You are an autonomous AI agent working in a crew.
        Your background: ${data.backstory}.
        You need to solve the following task:
        ${data.task}.
        The task solution must be presented like following:
        ${data.resultFormat}.
        You have the following tools at your disposal:
        $toolsPrompt
        $delegationPrompt
        You need to come up with a plan on how to solve this task.
        Present it in a JSON object like that: { "reason": "<reason>", "plan": ["task 1", "task 2"] }
        If you think that the task can't be accomplished given the resources you have, you must write "IMPOSSIBLE: <reason>"
    """.trimIndent()
}

fun composeDelegationPrompt(underlyingMembers: List<CrewMemberPromptData>): String {
    if (underlyingMembers.isEmpty()) return ""
    return buildString {
        appendLine("You can delegate to the underlying crew members:")
        for (underlyingMember in underlyingMembers) {
            appendLine("- ${underlyingMember.name}, who is ${underlyingMember.shortDescription}. They are proficient with ${underlyingMember.toolNames.joinToString()}.")
        }
        // TODO the format
    }
}

fun composeToolsPrompt(tools: List<ToolSpec>) : String {
    if (tools.isEmpty()) return ""
    return buildString {
        appendLine("You can use the following tools:")
        for (tool in tools) {
            appendLine("- ${tool.name}, which is ${tool.description}. It has the following parameters: ${tool.params.joinToString { it.name + " - " + it.description }}.")
        }
    }
}

@Serializable
class PlanResponse(
    val reason: String,
    val plan: List<String>
)

/// ----
val data = ListPlanPromptData(
    task = "Create a RESTful microservice for a movie recommendation system. This service will interact with a database and an external movie API. It should be able to list, add, update, and delete movies, as well as provide user-specific recommendations based on their previous interactions.",
    resultFormat = "The solution must be written in Kotlin and adhere to standard coding practices. The final result should be a JSON representation of the created, updated, or deleted movie, or the list of recommended movies.",
    backstory = "You are proficient with Kotlin, microservices architecture, and working with APIs and databases. You understand the basics of machine learning for recommendation systems.",
    tools = listOf(
        ToolSpec(
            id = "1",
            name = "getApiData",
            description = "A tool to fetch data from an API.",
            params = listOf(
                ParamSpec("endpoint", "The endpoint to which the request should be sent."),
                ParamSpec("method", "The HTTP method to be used in the request.")
            )
        ),
        ToolSpec(
            id = "2",
            name = "writeToFile",
            description = "A tool to write data to a file.",
            params = listOf(
                ParamSpec("fileName", "The name of the file where the data should be written."),
                ParamSpec("data", "The data to be written to the file.")
            )
        ),
        ToolSpec(
            id = "3",
            name = "getDatabaseData",
            description = "A tool to retrieve data from a database.",
            params = listOf(
                ParamSpec("query", "The SQL query to be executed."),
            )
        ),
        ToolSpec(
            id = "4",
            name = "calculateRecommendations",
            description = "A tool to calculate movie recommendations based on a user's history.",
            params = listOf(
                ParamSpec("userId", "The ID of the user for whom to calculate recommendations.")
            )
        )
    ),
    underlyingMembers = listOf(
        CrewMemberPromptData(
            name = "Database Engineer",
            toolNames = listOf("getDatabaseData"),
            shortDescription = "Specialist in interacting with databases"
        ),
        CrewMemberPromptData(
            name = "Backend Developer",
            toolNames = listOf("getApiData", "calculateRecommendations"),
            shortDescription = "Specialist in interfacing with APIs and calculating recommendations"
        ),
        CrewMemberPromptData(
            name = "Data Engineer",
            toolNames = listOf("writeToFile"),
            shortDescription = "Specialist in writing data to storage"
        )
    )
)

fun main() {
    println(composePlanPrompt(data))
}