package newImpl.model.execution

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import newImpl.model.*
import java.util.UUID

fun convertToExecutionGraph(graph: GraphSnapshot, currentElement: SmartPsiElementPointer<PsiElement>) : ExecutionGraph {
    val executionNodes = mutableMapOf<UUID, ExecutionNode>()
    val fromPortIdToEdge = mutableMapOf<UUID, MutableList<Edge>>()
    for (edge in graph.edges.values) {
        fromPortIdToEdge.computeIfAbsent(edge.fromPort) { mutableListOf() }.add(edge)
    }
    for (node in graph.nodes.values) {
        val content: NodeContent = graph.getContent(node.id)
        when (content) {
            is InputElement -> {
                val function = InputElementFunction(currentElement)
                val outputId = getSingleOutputId(graph, node, fromPortIdToEdge)
                val outputs = mutableMapOf(InputElementFunction.OUTPUT_ELEMENT to outputId)
                executionNodes[node.id] = ExecutionNode(function, mutableMapOf(), outputs)
            }
            is Printer -> {
                val function = PrintFunction()
                executionNodes[node.id] = ExecutionNode(function, mutableMapOf(PrintFunction.INPUT to null), mutableMapOf())
            }
            is FindUsages -> {
                val function = FindUsagesFunction()
                val outputId = getSingleOutputId(graph, node, fromPortIdToEdge)
                executionNodes[node.id] = ExecutionNode(
                    function,
                    mutableMapOf(FindUsagesFunction.INPUT_DEFINITION to null),
                    mutableMapOf(FindUsagesFunction.OUTPUT_REFERENCES to outputId)
                )
            }
            is ElementToSnippetConverter -> {
                val function = SnippetMakerFunction()
                val outputId = getSingleOutputId(graph, node, fromPortIdToEdge)
                executionNodes[node.id] = ExecutionNode(
                    function,
                    mutableMapOf(SnippetMakerFunction.INPUT_ELEMENT to null),
                    mutableMapOf(SnippetMakerFunction.OUTPUT_TEXT to outputId)
                )
            }
//            is FindUsages -> {
//                val FindUsage
//            }
//            is DiffApplier -> {
//                val function = PrintFunction()
//                executionNodes[node.id] = ExecutionNode(function, mutableMapOf("Diff" to ), outputs)
//            }
            else -> TODO()
        }
    }
    return ExecutionGraph(executionNodes)
}

private fun getSingleOutputId(
    graph: GraphSnapshot,
    node: Node,
    fromPortIdToEdge: MutableMap<UUID, MutableList<Edge>>
): OutputId {
    val outputPorts = graph.getOutputPorts(node.id)
    require(outputPorts.size == 1)
    val outputPort = outputPorts.first()
    val edge = fromPortIdToEdge[outputPort.id]!!.single()
    val toNode = edge.toNode
    val toPortName = graph.inputPortIdToPort[edge.toPort]!!.name
    val outputId = OutputId(toNode, toPortName)
    return outputId
}