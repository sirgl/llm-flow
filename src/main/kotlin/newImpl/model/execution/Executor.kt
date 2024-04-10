package newImpl.model.execution

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.parents
import kotlinx.coroutines.runBlocking
import newImpl.getPrompt
import newImpl.getResponseWithRetries
import java.util.*
import kotlin.random.Random

class ExecutionGraph(
    val nodes: Map<UUID, ExecutionNode>,
) {

    fun execute() {
        var steps = 0
        while (true) {
            steps++
            if (!doStep()) {
                break
            }
        }
        println(steps)
    }

    // TODO make reporting
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

                if (result != null) {
                    if (result.values.isNotEmpty()) {
                        for ((outputName, value) in result.values) {
                            val outputId = node.outputs[outputName]!!
                            // setting to input of out node
                            nodes[outputId.nodeId]!!.inputValues[outputId.inputName] = value
                        }
                        anythingExecuted = true
                    }
                    if (result.values.isEmpty() || result.consumesInputs) {
                        // cleaning used values
                        for (key in node.inputValues.keys.toList()) {
                            node.inputValues[key] = null
                        }
                    }
                } else {
                    // cleaning used values
                    for (key in node.inputValues.keys.toList()) {
                        node.inputValues[key] = null
                    }
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
    abstract fun execute(inputs: Map<String, ExecutionValue>): FunctionResult?
}

class FunctionResult(val values: Map<String, ExecutionValue?>, val consumesInputs: Boolean)

data class RandomProviderFunction(var count: Int = 1) : NodeFunction(listOf(OUTPUT)) {
    companion object {
        const val OUTPUT: String = "numbers"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
        if (count <= 0) {
            return null
        }
        count -= 1
        return FunctionResult(mapOf(OUTPUT to StringValue(Random.nextInt(1000000000).toString())), true)

    }
}

class ConcatFunction : NodeFunction(listOf(OUTPUT)) {
    companion object {
        const val INPUT_LEFT: String = "left"
        const val INPUT_RIGHT: String = "right"
        const val OUTPUT: String = "out"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
        val left = (inputs[INPUT_LEFT] as StringValue).value
        val right = (inputs[INPUT_RIGHT] as StringValue).value
        return FunctionResult(mapOf(OUTPUT to StringValue(left + " " + right)), true)
    }
}

class PrintFunction : NodeFunction(listOf()) {
    companion object {
        const val INPUT: String = "text"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
        val value = inputs[INPUT]
        println(value)
        return null
    }
}

data class LLmSnippetTransformer(val prompt: String) : NodeFunction(listOf(OUTPUT_SNIPPET)) {
    companion object {
        const val INPUT_SNIPPET: String = "Text"
        const val OUTPUT_SNIPPET: String = "Output"
    }
    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult {
        val value = inputs[INPUT_SNIPPET] as SnippetValue
        val finalPrompt = getPrompt(value.textWithLineNumbers, prompt)
        println("Prompt: $prompt")
        val replacement = runBlocking {
            getResponseWithRetries(
                finalPrompt, maxRetries = 5
            )?.replacement
        } ?: error("LLM failed")
        return FunctionResult(mapOf(OUTPUT_SNIPPET to SnippetValue(replacement, value.range, value.file)), true)
    }
}

data class FileSwitch(val extension: String) : NodeFunction(listOf()) {
    companion object {
        const val INPUT_SNIPPET: String = "snippet"
        const val OUTPUT_TARGET_FILE_SNIPPET: String = "snippet"
        const val OUTPUT_NON_TARGET_FILE_SNIPPET: String = "snippet"
    }
    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
        TODO("Not yet implemented")
    }
}

//data class AllInheritors(val extension: String) : NodeFunction(listOf()) {
//    companion object {
//        const val INPUT_SNIPPET: String = "snippet"
//        const val OUTPUT_TARGET_FILE_SNIPPET: String = "snippet"
//        const val OUTPUT_NON_TARGET_FILE_SNIPPET: String = "snippet"
//    }
//    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
//        TODO("Not yet implemented")
//    }
//}

class InputElementFunction(private val pointer: SmartPsiElementPointer<PsiElement>) :
    NodeFunction(listOf(OUTPUT_ELEMENT)) {
    companion object {
        const val OUTPUT_ELEMENT = "element"
    }

    private var counter = 1

    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
        if (counter <= 0) {
            return null
        }
        counter--
        return FunctionResult(mapOf(OUTPUT_ELEMENT to PsiElementValue(pointer)), true)
    }
}

class FindUsagesFunction : NodeFunction(listOf(OUTPUT_REFERENCES)) {
    companion object {
        const val INPUT_DEFINITION = "PSI definition"
        const val OUTPUT_REFERENCES = "PSI references"
    }

    private var iterator: Iterator<PsiReference>? = null

    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
//        println("Find usages")
        val referenceIterator = iterator
        if (referenceIterator == null) {
            val pointer = (inputs[INPUT_DEFINITION] as PsiElementValue).pointer
            val element = pointer.element ?: error("Element is not restoring")
            val identifierOwner = element.parents(true).filterIsInstance<PsiNameIdentifierOwner>().first()
            ProgressManager.getInstance().runProcess({
                iterator = ReferencesSearch.search(identifierOwner).iterator()
            }, EmptyProgressIndicator())
            return getNext(iterator!!)
        } else {
            if (!referenceIterator.hasNext()) {
                return null
            }
            return getNext(referenceIterator)
        }
    }

    private fun getNext(referenceIterator: Iterator<PsiReference>): FunctionResult? {
        var reference: PsiReference? = null
        val emptyProgressIndicator = EmptyProgressIndicator()
        ProgressManager.getInstance().runProcess({
            reference = referenceIterator.next()
        }, emptyProgressIndicator)

        val pointer = SmartPointerManager.createPointer(reference!!.element)
        return FunctionResult(mapOf(OUTPUT_REFERENCES to PsiElementValue(pointer)), false)
    }
}

class SnippetMakerFunction : NodeFunction(listOf(OUTPUT_TEXT)) {
    companion object {
        const val INPUT_ELEMENT = "PSI element"
        const val OUTPUT_TEXT = "Snippet"
    }
    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult {
        val pointer = (inputs[INPUT_ELEMENT] as PsiElementValue).pointer
        val element = pointer.element!!
        val virtualFile = pointer.virtualFile
        val (textWithLineNumbers, range) = getTextWithSurroundingLines(element)
        return FunctionResult(mapOf(OUTPUT_TEXT to SnippetValue(textWithLineNumbers, range, virtualFile)), true)
    }


    private fun getTextWithSurroundingLines(element: PsiElement, numberOfLines: Int = 5): Pair<String, TextRange> {
        // Fetch the document
        val documentManager = PsiDocumentManager.getInstance(element.project)
        val document = documentManager.getDocument(element.containingFile) ?: error("Not found document")

        // Extract line numbers
        val elementLine = document.getLineNumber(element.textOffset)
        val startLine = (elementLine - numberOfLines).coerceAtLeast(0)
        val endLine = (elementLine + numberOfLines + 1).coerceAtMost(document.lineCount)

        // Determine the text offset range
        val startOffset = document.getLineStartOffset(startLine)
        val endOffset = if (endLine < document.lineCount)
            document.getLineEndOffset(endLine)
        else
            document.textLength

        // Find the text within the calculated range
        val range = TextRange(startOffset, endOffset)
        val text = document.getText(range)

        // Calculate padding for line numbers
        val lineNumbers = (startLine..endLine).toList()
        val maxLineNumberLength = lineNumbers.maxOrNull().toString().length

        // Create prefixes with line numbers
        val lineNumberPrefixes = lineNumbers.map { it.toString().padEnd(maxLineNumberLength) }

        // Add line numbers to lines
        return text.lines().zip(lineNumberPrefixes) { line, number -> "$number: $line" }.joinToString("\n") to range
    }

}

class SnippetApplier(private val project: Project) : NodeFunction(listOf()) {
    companion object {
        const val INPUT_SNIPPET = "PSI element"
    }

    override fun execute(inputs: Map<String, ExecutionValue>): FunctionResult? {
        val snippetValue = inputs[INPUT_SNIPPET] as SnippetValue
        val textWithoutLineNumbers = removeLineNumbers(snippetValue.textWithLineNumbers)

        replaceRangeWithText(snippetValue.range, textWithoutLineNumbers, snippetValue.file)

        return null
    }

    private fun replaceRangeWithText(range: TextRange, newText: String, file: VirtualFile) {
        val documentManager = FileDocumentManager.getInstance()
        val document = documentManager.getDocument(file) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(range.startOffset, range.endOffset, newText)
        }
    }

    private fun removeLineNumbers(text: String): String {
        // Regular expression to find and remove line numbers
        val regex = Regex("""^\d+\s*:\s""")
        return text.lineSequence()
            .map { it.replace(regex, "") } // replace line numbers with empty strings
            .joinToString("\n") // join lines back together
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

data class StringValue(val value: String) : ExecutionValue()

data class PsiElementValue(val pointer: SmartPsiElementPointer<PsiElement>) : ExecutionValue()

data class SnippetValue(val textWithLineNumbers: String, val range: TextRange, val file: VirtualFile) : ExecutionValue()