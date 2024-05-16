package sirgl.model.agent.execution

import kotlinx.coroutines.isActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import sirgl.model.agent.AgentSpec
import sirgl.model.agent.execution.prompts.*
import kotlin.coroutines.coroutineContext

// TODO provide access to tools?
class AIAgent(val agentSpec: AgentSpec) {
    var state: ExecutionState = Initial
        private set
    private var history = History(ExecutionSectionLog("", SectionType.TopLevel, io.lacuna.bifurcan.List(), "", ""))

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
                        Json.Default.decodeFromString<PlanResponse>(string = response, deserializer = Json.serializersModule.serializer<PlanResponse>())
                    } catch (e: SerializationException) {
                        // TODO add to log
                        e.message
                        null
                    }
                    if (planResponse == null) {
                        TODO()
                    } else {
                        val plan: List<String> = planResponse.plan
                        history = History(history.sectionLog.withEntries(history.sectionLog.entries.addLast(PlanningExecutionEntry(plan))))
                        // TODO add to log
                        state = PlanPresented
                    }
                    // TODO execute and parse prompt
                    // TODO handle edge cases
                    // TODO should we update status? Should there be subroutines? or substatuses?
                }
                PlanPresented -> {
                    // TODO
                    val prompt = composeDecisionPrompt(
                        DecisionPromptData(
                            task, agentSpec.backstory, agentSpec.tools, listOf(), history
                        )
                    )
                    val response = executionContext.llmSet.getGeneric().request(prompt)
                    val choice = json.decodeFromString<ExecutionChoice>(response)
                    when (choice) {
                        is ExecutionChoice.Delegate -> TODO()
                        is ExecutionChoice.Finish -> TODO()
                        is ExecutionChoice.Impossible -> TODO()
                        is ExecutionChoice.Reasoning -> TODO()
                        is ExecutionChoice.Tool -> TODO()
                    }

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