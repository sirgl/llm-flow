package newImpl.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import newImpl.model.*
import newImpl.vm.GraphVM
import org.jetbrains.jewel.ui.component.Text

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Workspace(vm: GraphVM, applyChanges: (changes: List<GraphChange>) -> Unit) {
    Box(
        Modifier.fillMaxHeight().width(1000.dp)
            .onPointerEvent(eventType = PointerEventType.Move, onEvent = {
                vm.cursorPosition.value = it.changes.first().position
            })
    ) {
        DragableScreen {
            Column {
                Text(vm.cursorPosition.value.toString(), fontSize = 10.sp)
                Text("Nodes: " + vm.nodes.value.size, fontSize = 10.sp)
            }
            ContextMenuArea(items = {
                getContextItems(vm, applyChanges)
            }) {
                Box(Modifier.fillMaxSize()) {
                    for (node in vm.nodes.value) {
                        Node(true, node, vm, applyChanges, vm.dragModel)
                    }
                }
            }

            Edges(vm)
        }
    }
}

private fun getContextItems(
    vm: GraphVM,
    applyChanges: (changes: List<GraphChange>) -> Unit
) = listOf(
    ContextMenuItem(
        label = "AI transformer",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("AI transformer")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf("Text"),
                        outputPorts = listOf("Output"),
                        content = AITransformer("My prompt")
                    )
                )
            )
        }),
    ContextMenuItem(
        label = "Input PSI element",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("Input PSI element")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf(),
                        outputPorts = listOf("PSI element"),
                        content = InputElement
                    )
                )
            )
        }),
    ContextMenuItem(
        label = "Find usages",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("Find usages")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf("PSI definition"),
                        outputPorts = listOf("PSI references"),
                        content = FindUsages
                    )
                )
            )
        }),
    ContextMenuItem(
        label = "Debug printer",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("Debug printer")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf("text"),
                        outputPorts = listOf(),
                        content = Printer
                    )
                )
            )
        }),
    ContextMenuItem(
        label = "Diff converter",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("Diff converter")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf( "New text"),
                        outputPorts = listOf("Diff"),
                        content = DiffMaker
                    )
                )
            )
        }),
    ContextMenuItem(
        label = "Diff applier",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("Diff applier")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf("Diff"),
                        outputPorts = listOf(),
                        content = DiffApplier
                    )
                )
            )
        }),
    ContextMenuItem(
        label = "PSI to snippet",
        onClick = {
            val nodeName = vm.graph.current.suggestNewNodeName("PSI to snippet")
            applyChanges(
                listOf(
                    AddNode(
                        name = nodeName,
                        position = vm.cursorPosition.value,
                        inputPorts = listOf("PSI element"),
                        outputPorts = listOf("Snippet"),
                        content = ElementToSnippetConverter
                    )
                )
            )
        })
)

@Composable
private fun Edges(vm: GraphVM) {
    Canvas(Modifier.fillMaxSize()) {
        val startOffset = vm.dragModel.startOffset
        if (vm.dragModel.isDragging && startOffset != null) {
            val paint = Paint()
            paint.strokeWidth = 5f
            paint.color = Color.Blue
            paint.style = PaintingStyle.Stroke

            val path = org.jetbrains.skia.Path()
            path.apply {
                val startX = startOffset.x + 5.dp.toPx()
                val startY = startOffset.y + 5.dp.toPx()
                moveTo(startX, startY)
                val cursor = vm.cursorPosition.value
                cubicTo(startX + 100, startY, cursor.x - 100, cursor.y, cursor.x, cursor.y)
            }

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawPath(path, paint.asFrameworkPaint())
            }
        }


        val paint = Paint()
        paint.strokeWidth = 5f
        paint.color = Color.Black
        paint.style = PaintingStyle.Stroke
        for (edgeVM in vm.edges.value) {
            val start = edgeVM.getStart(this@Canvas)
            val startX = start.x
            val startY = start.y
            val end = edgeVM.getEnd(this@Canvas)
            val endX = end.x
            val endY = end.y

            // Calculate control points for the Bezier curve.
            val controlPoint1X = startX + (endX - startX) / 3
            val controlPoint1Y = startY
            val controlPoint2X = startX + 2 * (endX - startX) / 3
            val controlPoint2Y = endY

            val path = Path().apply {
                moveTo(startX, startY)
                cubicTo(controlPoint1X, controlPoint1Y, controlPoint2X, controlPoint2Y, endX, endY)
            }

            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawPath(path.asSkiaPath(), paint.asFrameworkPaint())
            }
            // TODO draw edge

        }
    }
}


class DragModelImpl(private val onDragEnd: (inputPortId: PortId) -> Unit) : DragModel {
    private val _isDragging = mutableStateOf(false)
    override var isDragging: Boolean
        get() = _isDragging.value
        set(value) {
            if (!value) {
                val portId = inputPortId
                if (portId != null) {
                    onDragEnd(portId)
                }
            }
            _isDragging.value = value
        }

    override var startOffset: Offset? by mutableStateOf(null)
    override var inputPortId: PortId? by mutableStateOf(null)

}

fun main() {
    val graph = Graph()
    val vm = GraphVM(graph)
    application {
        println("App recreated")
        Window(onCloseRequest = ::exitApplication) {
            Workspace(vm = vm, applyChanges = { changes ->
                graph.update(changes)
            })
        }
    }
}