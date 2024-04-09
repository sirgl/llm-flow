package newImpl.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import newImpl.model.AddEdge
import newImpl.model.Graph
import newImpl.ui.DragModelImpl
import newImpl.ui.PortId
import java.util.*

class GraphVM(val graph: Graph) {
    private val id = UUID.randomUUID()
    val nodes: MutableState<List<NodeVM>> = mutableStateOf(mutableListOf())
    val edges: MutableState<List<EdgeVM>> = mutableStateOf(mutableListOf())
    private var _hoveredInputPortId: MutableState<PortId?> = mutableStateOf(null)

    var hoveredInputPortId: PortId?
        get() = _hoveredInputPortId.value
        set(value) {
//            if (_hoveredInputPortId.value != value) {
//                println("HoveredInputPortId: Value changed to $value")
//            }
            _hoveredInputPortId.value = value
        }

    val dragModel = DragModelImpl(onDragEnd = { outputPortId ->
//        println("Connected from $outputPortId to $hoveredInputPortId")
        val inputPortId = hoveredInputPortId
        if (inputPortId != null) {
            graph.update(listOf(AddEdge(outputPortId.nodeId, outputPortId.portId, inputPortId.nodeId, inputPortId.portId)))
        }
        hoveredInputPortId = null
    })


    val cursorPosition = mutableStateOf(Offset.Zero)

    init {
        graph.subscribeNodeAdded { node, inputs, outputs, content ->
            val nodeVM = NodeVM(
                node.id, graph, inputs.map { InputPortVM(it.name, it.id, node.id) }, outputs.map { OutputPortVM(it.name, it.id, node.id) })
            nodes.value += nodeVM
        }
        graph.subscribeEdgeAdded {
            edges.value += EdgeVM(it, graph)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GraphVM) return false
        return other.id == id
    }

    override fun hashCode(): Int {
        throw IllegalStateException() // not expected to be called
    }
}

