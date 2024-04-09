package newImpl.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import newImpl.model.Graph
import newImpl.model.NodeContent
import newImpl.model.NodeListener
import java.util.*

class NodeVM(
    val nodeId: UUID,
    private val graph: Graph,
    val inputs: List<InputPortVM>,
    val outputs: List<OutputPortVM>
) {
    private val offset = Cached(Offset.Zero) { graph.current.getNode(nodeId).offset }
    private val name = Cached("") { graph.current.getNode(nodeId).name }
    private val content = Cached(null) { graph.current.getContent(nodeId) }
    private val id = Cached(
        UUID.nameUUIDFromBytes(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0))
    ) { graph.current.getNode(nodeId).id }

    fun getName(): String {
        return name.value
    }

    fun getOffset(): Offset {
        return offset.value
    }

    fun getId(): UUID {
        return id.value
    }

    fun getContent() : NodeContent {
        return content.value!!
    }


    init {
        // TODO remove listener on disposal
        graph.subscribe(nodeId, object : NodeListener {
            override fun onChanged() {
                dropCaches(offset, name, id, content)
                println("Changed")
            }
        })
    }

}

class Cached<T>(initial: T, private val getter: () -> T) {
    private val content: MutableState<T> = mutableStateOf(initial)

    private var isCached = false

    val value: T
        get() {
            if (isCached) {
                return content.value
            } else {
                val newValue = getter()
                content.value = newValue
                isCached = true
                return newValue
            }
        }

    fun dropCache() {
        isCached = false
    }
}

fun dropCaches(vararg cached: Cached<*>) {
    for (c in cached) {
        c.dropCache()
    }
}