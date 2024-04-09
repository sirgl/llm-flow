package newImpl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import newImpl.model.AITransformer
import newImpl.model.GraphChange
import newImpl.model.ReplaceContent
import newImpl.model.UpdateNode
import newImpl.vm.GraphVM
import newImpl.vm.InputPortVM
import newImpl.vm.NodeVM
import newImpl.vm.OutputPortVM
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun Node(isDebug: Boolean, vm: NodeVM, graphVM: GraphVM, applyChanges: (changes: List<GraphChange>) -> Unit, dragModel: DragModel) {
    Box(
        Modifier
            .offset { IntOffset(vm.getOffset().x.roundToInt(), vm.getOffset().y.roundToInt()) }
            .width(150.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.LightGray)
            .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    applyChanges(listOf(UpdateNode(newImpl.model.Node(vm.nodeId, vm.getName(), vm.getOffset() + dragAmount))))
                }
            }
    ) {
        Box(Modifier.padding(10.dp)) {
            Column {
                Text(vm.getName(), style = TextStyle.Default.copy(fontWeight = FontWeight.Bold))
                if (isDebug) {
                    Text(text = vm.getId().toString(), fontSize = 6.sp)
                }
                Divider(Orientation.Horizontal)
                for (input in vm.inputs) {
                    InputPort(input, graphVM)
                }
                Divider(Orientation.Horizontal)
                for (output in vm.outputs) {
                    OutputPort(output, dragModel)
                }
                Divider(Orientation.Horizontal)
                val content = vm.getContent()
                if (content is AITransformer) {
                    // TODO here should be just a link to edit
                    var prompt by remember { mutableStateOf(content.prompt) }
                    TextField(value = prompt, { prompt = it })
                    DefaultButton(onClick = {
                        applyChanges(listOf(ReplaceContent(vm.nodeId, AITransformer(prompt))))
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun OutputPort(input: OutputPortVM, dragModel: DragModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // This will place the elements on opposite ends
        verticalAlignment = Alignment.CenterVertically     // This will center-align the elements vertically
    ) {
        Text(input.name)
        val portId = PortId(input.nodeId, input.portId)
        DragTarget(Modifier, portId, dragModel, onDragStart = {
            dragModel.inputPortId = portId
        }) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            )
        }
    }
}

@Composable
private fun InputPort(input: InputPortVM, graphVM: GraphVM) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // This will place the elements on opposite ends
        verticalAlignment = Alignment.CenterVertically     // This will center-align the elements vertically
    ) {
        DropItem<PortId>(Modifier) { isInBound, _ ->
            if (isInBound) {
                graphVM.hoveredInputPortId = PortId(input.nodeId, input.id)
            }

            val color = if (isInBound) Color.Blue else Color.DarkGray
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(input.name)

    }
}

data class PortId(val nodeId: UUID, val portId: UUID)