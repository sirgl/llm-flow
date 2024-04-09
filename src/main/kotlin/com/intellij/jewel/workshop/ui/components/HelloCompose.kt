package com.intellij.jewel.workshop.ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import com.intellij.jewel.workshop.ui.preview.IntUiPreview
import newImpl.model.Graph
import newImpl.ui.Workspace
import newImpl.vm.GraphVM

@Composable
fun GraphPanel() {
    val graph = Graph()
    val vm = GraphVM(graph)
    Workspace(vm = vm, applyChanges = { changes ->
        graph.update(changes)
    })
//    Box(Modifier.padding(8.dp)) {
//        Text("Hello Compose!")
//    }
}

@Preview
@Composable
fun HelloComposePreview() = IntUiPreview(isDark = true) {
    GraphPanel()
}