package sirgl.model.agent.execution

import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import sirgl.model.agent.AgentSpec
import java.util.*

class ExecutionEngine {
    private val dispatcher: CoroutineDispatcher = newSingleThreadContext("Execution")
    private val scope = CoroutineScope(dispatcher)

    // TODO execution environment
    fun prepareTask(project: Project, task: String, resultFormat: String) : ExecutableTask {
        TODO()
    }

    fun execute(task: ExecutableTask) : Job {
        return scope.launch(CoroutineName("Execute LLM task")) {
            val agents = task.crew.agents.map { AIAgent(it) }
            val topLevelManagerName = task.crew.topLevelManagerName
            val topLevelAgent = agents.find { it.agentSpec.name == topLevelManagerName } ?: error("Top level agent with name \"$topLevelManagerName\" not found")
            topLevelAgent.executeTask(task.context, task.task)
        }
    }
}

class ExecutableTask(
    val task: AITask,
    val id: UUID,
    // TODO log storage
//    val logFlow: SharedFlow<>,
    val crew: Crew,
    val context: ExecutionContext
) {
}

class Crew(
    val topLevelManagerName: String,
    val agents: List<AgentSpec>,
    val agentDelegates: Map<String, String>,
)

class FSSnapshot // potentially possible

// TODO preferably bake everything needed into the environment so that it is self sufficient