package newImpl.model.execution

import java.util.*
import kotlin.random.Random

class ExecutionGraph(
    val nodes: Map<UUID, ExecutionNode>,
) {

    fun doStep(): Boolean {
        var anythingExecuted = false
        for (node in nodes.values) {
            val inputValues = node.inputValues
            if (inputValues.values.all { it != null }) {
                // checking if there are free places in output nodes
                if (!node.function.outputs.all {
                    val outputId = node.outputs[it]!!
                    val nodeToPushResultTo = nodes[outputId.nodeId]!!
                    nodeToPushResultTo.inputValues[outputId.inputName] == null
                }) {
                    continue
                }


                val result = node.function.execute(inputValues.mapValues { it.value!! })


                // cleaning used values
                for (key in node.inputValues.keys.toList()) {
                    node.inputValues[key] = null
                }

                if (result.isNotEmpty()) {
                    for ((outputName, value) in result) {
                        val outputId = node.outputs[outputName]!!
                        // setting to input of out node
                        nodes[outputId.nodeId]!!.inputValues[outputId.inputName] = value
                    }
                    anythingExecuted = true
                }
            }
        }
        return anythingExecuted
    }
}

data class OutputId(
    val nodeId: UUID,
    val inputName: String
)

class ExecutionNode(
    val function: NodeFunction,
    val inputValues: MutableMap<String, ExecutionValue?>,
    val outputs: MutableMap<String, OutputId>
)

// TODO names of present inputs and outputs
sealed class NodeFunction(val outputs: List<String>) {
    abstract fun execute(inputs: Map<String, ExecutionValue>): Map<String, ExecutionValue?>

}

data class RandomProviderFunction(var count: Int = 1) : NodeFunction(listOf(OUTPUT)) {
    companion object {
        const val OUTPUT: String = "numbers"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): Map<String, ExecutionValue?> {
        if (count <= 0) {
            return emptyMap()
        }
        count -= 1
        return mapOf(OUTPUT to StringValue(Random.nextInt(1000000000).toString()))

    }
}

class ConcatFunction : NodeFunction(listOf(OUTPUT)) {
    companion object {
        const val INPUT_LEFT: String = "left"
        const val INPUT_RIGHT: String = "right"
        const val OUTPUT: String = "out"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): Map<String, ExecutionValue?> {
        val left = (inputs[INPUT_LEFT] as StringValue).value
        val right = (inputs[INPUT_RIGHT] as StringValue).value
        return mapOf(OUTPUT to StringValue(left + " " + right))
    }
}

class PrintFunction : NodeFunction(listOf()) {
    companion object {
        const val INPUT: String = "text"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): Map<String, ExecutionValue?> {
        val value = (inputs[INPUT] as StringValue).value
        println(value)
        return emptyMap()
    }
}

data class LLmFunction(val prompt: String, val llm: LLM) : NodeFunction(listOf()) {
    override fun execute(inputs: Map<String, ExecutionValue>): Map<String, ExecutionValue?> {
        TODO()
    }
}

class LLM {
    fun request(prompt: String): String {
        TODO()
    }
}

fun main() {
    val input1Node = UUID.randomUUID()
    val input2Node = UUID.randomUUID()
    val concatNode = UUID.randomUUID()
    val printNode = UUID.randomUUID()
    val graph = ExecutionGraph(
        mapOf(
            input1Node to ExecutionNode(
                RandomProviderFunction(2),
                mutableMapOf(),
                mutableMapOf(RandomProviderFunction.OUTPUT to OutputId(concatNode, ConcatFunction.INPUT_LEFT))
            ),
            input2Node to ExecutionNode(
                RandomProviderFunction(2),
                mutableMapOf(),
                mutableMapOf(RandomProviderFunction.OUTPUT to OutputId(concatNode, ConcatFunction.INPUT_RIGHT))
            ),
            concatNode to ExecutionNode(
                ConcatFunction(),
                mutableMapOf(ConcatFunction.INPUT_LEFT to null, ConcatFunction.INPUT_RIGHT to null),
                mutableMapOf(ConcatFunction.OUTPUT to OutputId(printNode, PrintFunction.INPUT)),
            ),
            printNode to ExecutionNode(
                PrintFunction(),
                mutableMapOf(PrintFunction.INPUT to null),
                mutableMapOf(),
            )
        )
    )
    var steps = 0
    while (true) {
        steps++
        if (!graph.doStep()) {
            break
        }
    }
    println(steps)
}

sealed class ExecutionValue

class StringValue(val value: String) : ExecutionValue()

class ListValue(val values: List<ExecutionValue>) : ExecutionValue()