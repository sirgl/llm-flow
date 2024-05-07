package sirgl.model.agent.execution

import kotlinx.coroutines.isActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import sirgl.model.agent.AgentSpec
import kotlin.coroutines.coroutineContext

// TODO provide access to tools?
class AIAgent(val agentSpec: AgentSpec) {
    var state: ExecutionState = Initial
        private set


    // TODO it should return the result
    suspend fun executeTask(executionContext: ExecutionContext, task: AITask) : String {
        while (true) {
            if (state.isTerminal) {
                break
            }
            if (coroutineContext.isActive) {
                // TODO put interrupted to log?
                break
            }
            when (state) {
                Initial -> {
                    // TODO technically, plan can be optional - opportunity to test, how good can it be without plan
                    val planPrompt = composePlanPrompt(
                        ListPlanPromptData(
                            task.text,
                            task.resultFormat,
                            agentSpec.backstory,
                            agentSpec.tools,
                            listOf()
                        )
                    )
                    val response = executionContext.llmSet.getGeneric().request(planPrompt)
                    // TODO make sure it is not impossible
                    val planResponse: PlanResponse? = try {
                        Json.decodeFromString(response)
                    } catch (e: SerializationException) {
                        // TODO add to log
                        e.message
                        null
                    }
                    if (planResponse == null) {
                        TODO()
                    } else {
                        val plan: List<String> = planResponse.plan
                        // TODO add to log
                        state = PlanPresented
                    }
                    // TODO execute and parse prompt
                    // TODO handle edge cases
                    // TODO should we update status? Should there be subroutines? or substatuses?
                }
                else -> {
                    TODO()
                }
            }
        }
        return TODO()
    }
}

sealed class ExecutionState(val isTerminal: Boolean = false)

data object Initial : ExecutionState()
data object PlanPresented: ExecutionState()
data object Finished: ExecutionState(isTerminal = true)
data object Unsolvable: ExecutionState(isTerminal = true)