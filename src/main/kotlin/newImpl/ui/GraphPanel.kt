package newImpl.ui

import actions.CurrentElementService
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import newImpl.model.Graph
import newImpl.model.GraphSnapshot
import newImpl.model.execution.convertToExecutionGraph
import newImpl.vm.GraphVM
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.component.*


@Composable
fun GraphPanel(project: Project) {
    val graph = Graph()
    val vm = GraphVM(graph)
    Row {
        Workspace(vm = vm, applyChanges = { changes ->
            graph.update(changes)
        })
        Divider(orientation = Orientation.Vertical)
        GraphRightPanel(graph, project)
    }
}

@Composable
fun GraphRightPanel(graph: Graph, project: Project) {
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
                            DefaultButton(onClick = {
                                AppExecutorUtil.getAppExecutorService().execute {
                                    runReadAction {
                                        val pointer = project.service<CurrentElementService>().currentElement
                                        if (pointer == null) error("Need to call AddInputAction before")

                                        val executionGraph = convertToExecutionGraph(project, graph.current, pointer)
                                        executionGraph.execute()
                                    }
                                }
                            }) {
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


fun getCurrentEditor(project: Project?): Editor? {
    // Get the FileEditorManager instance for the current project
    val fileEditorManager = FileEditorManager.getInstance(project!!)


    // Retrieve the currently opened file in the editor
    val currentFile = if (fileEditorManager.selectedFiles.size > 0) fileEditorManager.selectedFiles[0] else null
    if (currentFile == null) {
        return null // No file is currently opened
    }


    val document = FileDocumentManager.getInstance().getDocument(currentFile) ?: return null

    val editors: Array<Editor> = EditorFactory.getInstance().getEditors(document, project)


    // Return the first editor in the list, which is typically the currently active editor
    return if (editors.isNotEmpty()) editors[0] else null
}