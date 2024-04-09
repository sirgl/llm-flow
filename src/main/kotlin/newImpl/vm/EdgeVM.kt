package newImpl.vm

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import newImpl.model.Edge
import newImpl.model.Graph

class EdgeVM(private val edge: Edge, private val graph: Graph) {
    fun getStart(density: Density): Offset {
        val snapshot = graph.current
        val node = snapshot.getNode(edge.fromNode)
        val offset = node.offset
        val index = snapshot.getOutputPortIndex(edge.fromNode, edge.fromPort)
        val inputCount = snapshot.getInputCount(node.id)
        val relativeOffset = getOutputOffsetRelativeToNode(inputCount, index, density)
        return offset + relativeOffset
    }

    fun getEnd(density: Density): Offset {
        val node = graph.current.getNode(edge.toNode)
        val offset = node.offset
        val index = graph.current.getInputPortIndex(edge.toNode, edge.toPort)
        with(density) {
            return Offset(offset.x + 15.dp.toPx(), offset.y + 43.dp.toPx() + (index * 16).dp.toPx())
        }
    }
}

fun getOutputOffsetRelativeToNode(inputCount: Int, outputIndex: Int, density: Density) : Offset {
    with(density) {

        // Base offset seems to begin from 45 and increments by 16.dp for each inputCount
        val baseOffset = 45.dp + (inputCount * 16).dp
        // offset is always 135.dp in x direction
        val xOffset = 135.dp

        return Offset(xOffset.toPx(), baseOffset.toPx() + (outputIndex * 16).dp.toPx())
    }
}