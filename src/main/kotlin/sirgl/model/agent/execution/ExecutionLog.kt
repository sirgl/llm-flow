package sirgl.model.agent.execution

import io.lacuna.bifurcan.IList

interface ExecutionLogger {
    fun updateSectionLog(newLog: ExecutionSectionLog)
    val current: ExecutionSectionLog
}

class ExecutionSectionLog(
    val sectionName: String,
    val sectionType: SectionType,
    val entries: IList<ExecutionEntry>,
    val task: String, // TODO it is not the case for tools request
    val responseFormat: String,
) {
    fun withEntries(newEntries: IList<ExecutionEntry>) =
        ExecutionSectionLog(sectionName, sectionType, newEntries, task, responseFormat)
}

enum class SectionType { // TODO likely classes are needed here
    Delegation,
    TopLevel,
    // TODO tool invoсation?
}

sealed class ExecutionEntry

class PlanningExecutionEntry(
    val plan: List<String>
) : ExecutionEntry()

class PlanningExecutionEntryRequest(
    val raw: String,
//    val
)

class History(
    val sectionLog: ExecutionSectionLog
)

// TODO All these entries should be writable to the disk

//interface LogEntity {
//    val id: UUID
//}
//
//interface TaskEntity : LogEntity {
//    val text: CharSequence
//}
//
//interface ExecutorLogEntity : LogEntity {
//    val task: TaskEntity // task from parent
//    val time: LocalDateTime
//    val inputs: InputsEntity
//    val executionEntries: IList<ExecutionEntity>
//    val revision: Long // modification count
//}
//
//sealed interface InputsEntity
//
//interface ObservableInputsEntity : InputsEntity {
//    val fileText: CharSequence
//    val fileName: CharSequence
//}
//
//
//
//sealed interface ExecutionEntity : LogEntity
//
//interface RequestResponsePairEntity : ExecutionEntity
//
//interface TaskRequest : ExecutionEntity {
//
//}
//
//interface TaskResponse : ExecutionEntity {
//
//}
//
//interface ToolRequest {
//    val toolId: CharSequence
//    val args: Map<CharSequence, Args>
//    val rawRequest: CharSequence
//    // TODO how looks like the handling of the tool afterwards?
//}
//
//interface ToolResponse {
//    val text: CharSequence
//    val toolLog: List<CharSequence>?
//    val toolResponseContent: ToolResponseContent
//}
//
//sealed interface ToolResponseContent : LogEntity {
//
//}
//
//interface ErrorInArguments : ToolResponseContent {
//
//}
//
//interface Log {
//    fun append(logEntity: LogEntity) // TODO it shouldn't be a log entity - we should not create it because of an id
//    // TODO there should be some modification mechanism
//    fun createSnapshot()
//    fun enterSection()
//    fun leaveSection(sectionId: UUID)
//}
//
//// TODO what is needed is to have mutable tree on top of immutable structure. Some kind of diff
//

