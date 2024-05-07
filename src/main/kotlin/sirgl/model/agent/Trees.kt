package sirgl.model.agent

import io.lacuna.bifurcan.IList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import java.util.UUID

interface TreeRoot {
    val element: TreeElement
    fun applyDiff(diff: TreeDiff)
}

interface TreeElement {
    val id: UUID
    val children: IList<TreeElement>
    val content: TreeContent
    fun createMutator() : MutableTreeElement
}

interface MutableTreeElement : TreeElement {
    fun asDiff() : TreeDiff
    fun replaceContent(id: UUID, newContent: TreeContent) : TreeContent
    // TODO edit functions
    fun addBeforeChild(id: UUID, newChild: TreeElement)
    fun addAfterChild(id: UUID, newChild: TreeElement)
    fun addLast(newChild: TreeElement)
    fun remove(id: UUID)
}


interface TreeDiff {
    // TODO diff
}

/**
 * Immutable content of the tree
 */
interface TreeContent

// ------------------ WRAPPERS (use trees only underneath) -------------

// root in a wrapper case
interface RedLogTree {
    val task: RedTask

}

interface RedTask {
    val text: CharSequence
}

sealed interface RedExecutionEntry

interface Red


// Technically should not be a part of the tree (it is one level above)
interface FullLog {
    val epochs: Sequence<LogEpoch>

    val current: SharedFlow<LogEpoch>
}

interface LogEpoch {
    val name: CharSequence?
    val treeRoot: RedLogTree
}