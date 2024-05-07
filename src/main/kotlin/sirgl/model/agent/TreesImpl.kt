//package sirgl.model.agent
//
//import io.lacuna.bifurcan.IList
//import java.util.*
//
//class TreeElementImpl(
//    override val id: UUID,
//    val _children: IList<TreeElement>,
//    override val content: TreeContent
//) : TreeElement {
//    override fun createMutator(): MutableTreeElement {
//        TODO("Not yet implemented")
//    }
//
//    override val children: List<TreeElement> = _children.toList()
//}
//
//
//// TODO this thing should create children lazily
//class MutableTreeElementImpl(override val id: UUID, override val content: TreeContent, val _children: IList<TreeElement>,) : MutableTreeElement {
//    init {
//
//    }
//
//    override val children: IList<MutableTreeElement> by lazy { _children.toList().map { MutableTreeElementImpl(it.id, it.content, it.children)} }
//
//    override fun asDiff(): TreeDiff {
//        TODO("Not yet implemented")
//    }
//
//    override fun replaceContent(id: UUID, newContent: TreeContent): TreeContent {
//        TODO("Not yet implemented")
//    }
//
//    override fun addBeforeChild(id: UUID, newChild: TreeElement) {
//        TODO("Not yet implemented")
//    }
//
//    override fun addAfterChild(id: UUID, newChild: TreeElement) {
//        TODO("Not yet implemented")
//    }
//
//    override fun addLast(newChild: TreeElement) {
//        TODO("Not yet implemented")
//    }
//
//    override fun remove(id: UUID) {
//        TODO("Not yet implemented")
//    }
//
//    override fun createMutator(): MutableTreeElement {
//        return this
//    }
//}