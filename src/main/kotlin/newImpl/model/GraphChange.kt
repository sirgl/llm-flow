package newImpl.model

import androidx.compose.ui.geometry.Offset
import java.util.*

sealed class GraphChange
class AddEdge(val fromNode: UUID, val fromPort: UUID, val toNode: UUID, val toPort: UUID) : GraphChange()
class AddNode(val name: String, val position: Offset, val inputPorts: List<String>, val outputPorts: List<String>, val content: NodeContent) : GraphChange()
class ReplaceContent(val nodeId: UUID, val newContent: NodeContent) : GraphChange()
class UpdateNode(val newNode: Node) : GraphChange()