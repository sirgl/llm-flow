package actions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer

@Service(Service.Level.PROJECT)
class CurrentElementService {
    var currentElement: SmartPsiElementPointer<PsiElement>? = null
}

class AddInputAction : IntentionAction {
    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getFamilyName(): String {
        return "Add input"
    }

    override fun getText(): String {
        return "Add input"
    }

    override fun isAvailable(p0: Project, editor: Editor, file: PsiFile): Boolean {
        return true
    }

    override fun invoke(p0: Project, editor: Editor, file: PsiFile) {
        val element = file.findElementAt(editor.caretModel.offset) ?: return
        val service = p0.service<CurrentElementService>()
        service.currentElement = SmartPointerManager.createPointer(element)
    }
}