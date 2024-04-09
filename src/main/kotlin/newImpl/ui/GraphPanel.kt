package newImpl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import newImpl.model.Graph
import newImpl.vm.GraphVM
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*

@Composable
fun GraphPanel() {
    val graph = Graph()
    val vm = GraphVM(graph)
    Row {
        Workspace(vm = vm, applyChanges = { changes ->
            graph.update(changes)
        })
        Divider(orientation = Orientation.Vertical)
        GraphRightPanel()
    }
}

@Composable
fun GraphRightPanel() {
    Box(Modifier.width(400.dp)) {
        val tabs by remember { mutableStateOf(listOf(" Node", " Execution")) }
        var selectedTabIndex by remember { mutableStateOf(0) }
        Column {
            TabStrip(
                tabs.mapIndexed { index, name ->
                    TabData.Default(
                        selected = tabs.indexOf(name) == selectedTabIndex,
                        name,
                        onClick = { selectedTabIndex = index })
                }
            )
            Box(Modifier.padding(10.dp)) {
                val currentTab = tabs[selectedTabIndex]
                when (currentTab) {
                    " Node" -> {
                        Text(" Node")
                    }
                    " Execution" -> {
                        Column {
                            DefaultButton(onClick = {}) {
                                Text("Execute")
                            }
                            // TODO here should be a place for a log
                        }
                    }
                    else -> {
                        TODO()
                    }
                }
            }
        }
    }
}