package newImpl.model

import androidx.compose.ui.geometry.Offset
import java.util.*

class Graph {
    var current: GraphSnapshot = GraphSnapshot()
    // TODO it would be better if these listeners would be more structured, like in datoms/workspace model
    private val nodeAddedListener = mutableListOf<(Node, List<InputPort>, List<OutputPort>, NodeContent) -> Unit>()
    private val edgeAddedListener = mutableListOf<(Edge) -> Unit>()
    private val nodeChangedListener = mutableMapOf<UUID, NodeListener>()
    private val edgeChangedListener = mutableMapOf<UUID, (Edge) -> Unit>()

    fun subscribe(nodeId: UUID, nodeListener: NodeListener) {
        require(nodeChangedListener.put(nodeId, nodeListener) == null)
    }

    fun subscribeNodeAdded(listener: (Node, List<InputPort>, List<OutputPort>, NodeContent) -> Unit) {
        nodeAddedListener.add(listener)
    }

    fun subscribeEdgeAdded(listener: (newEdge: Edge) -> Unit) {
        edgeAddedListener.add(listener)
    }

    fun update(changes: List<GraphChange>) {
        val snapshot = current
        val nodes = snapshot.nodes.toMutableMap()
        val edges = snapshot.edges.toMutableMap()
        val nodeContent = snapshot.nodeIdToContent.toMutableMap()
        val nodeToInputPortId = snapshot.nodeToInputPortId.toMutableMap()
        val nodeToOutputPortId = snapshot.nodeToOutputPortId.toMutableMap()
        val inputPortIdToPort = snapshot.inputPortIdToPort.toMutableMap()
        val outputPortIdToPort = snapshot.outputPortIdToPort.toMutableMap()
        for (change in changes) {
            when (change) {
                is AddEdge -> {
                    val edge = Edge(UUID.randomUUID(), change.fromNode, change.toNode, change.fromPort, change.toPort)
                    edges[edge.id] = edge

                    for (listener in edgeAddedListener) {
                        listener(edge)
                    }
                }

                is AddNode -> {
                    val uuid = UUID.randomUUID()
                    val node = Node(uuid, change.name, change.position)
                    nodes[uuid] = node

                    val content = change.content
                    nodeContent[uuid] = content

                    val inputPorts = change.inputPorts.map { InputPort(it, UUID.randomUUID()) }
                    nodeToInputPortId[uuid] = inputPorts.map { it.id }
                    for (inputPort in inputPorts) {
                        inputPortIdToPort[inputPort.id] = inputPort
                    }

                    val outputPorts = change.outputPorts.map { OutputPort(it, UUID.randomUUID()) }
                    nodeToOutputPortId[uuid] = outputPorts.map { it.id }
                    for (output in outputPorts) {
                        outputPortIdToPort[output.id] = output
                    }

                    for (listener in nodeAddedListener) {
                        listener(node, inputPorts, outputPorts, content)
                    }
                }
                is ReplaceContent -> {
                    val nodeId = change.nodeId
                    nodeContent[nodeId] = change.newContent
                    nodeChangedListener[nodeId]?.onChanged()
                }
                is UpdateNode -> {
                    val newNode = change.newNode
                    val newNodeId = newNode.id
                    nodes[newNodeId] = newNode
                    nodeChangedListener[newNodeId]?.onChanged()
                }
            }
        }

        current = GraphSnapshot(nodes, edges, nodeContent,
            nodeToInputPortId, nodeToOutputPortId, inputPortIdToPort, outputPortIdToPort)
    }
}

interface NodeListener {
    fun onChanged()
}

class InputPort(val name: String, val id: UUID)
class OutputPort(val name: String, val id: UUID)

class Node(val id: UUID, val name: String, val offset: Offset)

class Edge(val id: UUID, val fromNode: UUID, val toNode: UUID, val fromPort: UUID, val toPort: UUID)

sealed class NodeContent

data object InputElement : NodeContent()

data object Printer : NodeContent()

data object FindUsages : NodeContent()

data object ElementToSnippetConverter : NodeContent()

data class AITransformer(val prompt: String) : NodeContent()

data object DiffMaker : NodeContent()

data object DiffApplier : NodeContent()

