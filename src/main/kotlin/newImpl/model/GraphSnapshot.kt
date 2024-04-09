package newImpl.model

import java.util.*
import kotlin.collections.HashMap

class GraphSnapshot(
    val nodes: Map<UUID, Node> = HashMap(),
    val edges: Map<UUID, Edge> = HashMap(),
    val nodeIdToContent: Map<UUID, NodeContent> = HashMap(),
    val nodeToInputPortId: Map<UUID, List<UUID>> = HashMap(),
    val nodeToOutputPortId: Map<UUID, List<UUID>> = HashMap(),
    val inputPortIdToPort: Map<UUID, InputPort> = HashMap(),
    val outputPortIdToPort: Map<UUID, OutputPort> = HashMap(),
) {
    fun getOutputPorts(nodeId: UUID) : List<OutputPort> {
        val ids = nodeToOutputPortId[nodeId] ?: return emptyList()
        return ids.map { outputPortIdToPort[it]!! }
    }

    fun getInputPorts(nodeId: UUID) : List<InputPort> {
        val ids = nodeToInputPortId[nodeId] ?: return emptyList()
        return ids.map { inputPortIdToPort[it]!! }
    }

    fun suggestNewNodeName(baseName: String): String {
        if (nodes.none { it.value.name == baseName }) return baseName
        var i = 1
        while (true) {
            val modifiedName = baseName + i
            if (nodes.none { it.value.name == modifiedName }) return modifiedName
            i++
        }
    }

    fun getEdge(edgeId: UUID): Edge {
        return edges[edgeId]
            ?: error("Edge $edgeId not found")
    }

    fun getNode(nodeId: UUID): Node {
        return nodes[nodeId]!!
    }

    fun findNode(predicate: (Node, NodeContent) -> Boolean): Pair<Node, NodeContent>? {
        for (value in nodes.values) {
            val content = nodeIdToContent[value.id]!!
            if (predicate(value, content)) {
                return value to content
            }
        }
        return null
    }

    fun getOutputPortIndex(nodeId: UUID, outputPortId: UUID): Int {
        return nodeToOutputPortId[nodeId]!!.indexOf(outputPortId)
    }

    fun getInputPortIndex(nodeId: UUID, inputPortId: UUID): Int {
        return nodeToInputPortId[nodeId]!!.indexOf(inputPortId)
    }

    fun getInputCount(nodeId: UUID): Int {
        return nodeToInputPortId[nodeId]!!.size
    }

    fun getContent(nodeId: UUID): NodeContent {
        return nodeIdToContent[nodeId]!!
    }
}