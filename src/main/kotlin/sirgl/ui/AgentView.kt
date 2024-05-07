package sirgl.ui

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.jewel.ui.component.ButtonState
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import java.util.*

class AgentHierarchyItemVM(private val agentId: UUID, val name: String, val children: MutableList<AgentHierarchyItemVM>) {
    fun clickAdd() {
        TODO()
    }

    fun invoke() {
        TODO()
    }
}

@Composable
fun RecursiveItemView(item: AgentHierarchyItemVM, depth: Int = 0) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Row(
        Modifier
            .padding(start = depth * 16.dp)
            .hoverable(interactionSource = interactionSource)
    ) {
        Text(item.name)

        if (isHovered) {
            DefaultButton(onClick = { item.clickAdd() }) {
                Text("Button")
            }
        }
    }

    Column {
        for (child in item.children) {
            RecursiveItemView(child, depth + 1)
        }
    }
}