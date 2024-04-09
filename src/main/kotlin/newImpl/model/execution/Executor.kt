package newImpl.model.execution

import newImpl.model.GraphSnapshot

class Executor {
    suspend fun execute(graphSnapshot: GraphSnapshot) {
        // TODO find input
//        val (node, content) = graphSnapshot.findNode { node, content -> content is EntranceContent }!!
        // TODO traverse the graph and on each iteration try to push value


    }
}

class ExecutionGraph {

}

// TODO names of present inputs and outputs
sealed class ExecutionNode {
    val inputs: Map<String, ExecutionValue> = hashMapOf()
}

class TextConvertingNode : ExecutionNode() {

}

class InputExecutionNode : ExecutionNode()

class OutputExecutionNode : ExecutionNode()



sealed class ExecutionValue

class StringValue(val value: String) : ExecutionValue()

class ListValue(val values: List<ExecutionValue>) : ExecutionValue()

//suspend fun convert(inputs: Map<String, ExecutionValue>) : Map<String, ExecutionValue> {
//
//}