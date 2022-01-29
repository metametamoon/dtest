package extractor

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import extractAssertions
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import javax.script.ScriptEngineManager


val kotlinScriptEngine by lazy { ScriptEngineManager().getEngineByExtension("kts")!! }

fun extractTests(ktFile: KtFile): List<Test> {
    val docs = extractFunctionsAndDocs(ktFile)
    val document = ktFile.viewProvider.document
    val assertionSets = docs.map { (doc, function) ->
        val sourceLocation =
            extractSourceLocation(function, document, ktFile.name)
        ktFile.packageFqName.child(function.nameAsSafeName) to extractAssertions(
            doc,
            sourceLocation
        )
    }
    return assertionSets.map { (fqName, assertions) ->
        Test(fqName) {
            assertions.forEach { assertion ->
                assertion.execute(kotlinScriptEngine)
            }
        }
    }
}

private fun extractSourceLocation(
    element: PsiElement, document: Document?, fileName: String
): SourceLocation {
    return if (document == null) SourceLocation(fileName, null)
    else SourceLocation(
        fileName, document.getLineNumber(element.startOffset) + 1
    )
}