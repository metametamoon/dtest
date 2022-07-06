package tree.comparer

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull


private val PsiElement.childrenNoWhitespaces: List<PsiElement>
    get() {
        var currentChild: PsiElement? = firstChild
        val allChildren = mutableListOf<PsiElement>()
        while (true) {
            currentChild?.let { currentChildNonNull ->
                allChildren.add(currentChildNonNull)
                currentChild = currentChildNonNull.nextSibling
            } ?: break
        }
        return allChildren.filter { it !is PsiWhiteSpace }
    }


class TreeComparer(private val expectedDocument: Document, private val actualDocument: Document) {
    private fun PsiElement.getPosition(document: Document): PsiPosition {
        val lineNumber = document.getLineNumber(textOffset)
        return PsiPosition(
            lineNumber,
            textOffset - document.getLineStartOffset(lineNumber)
        )
    }

    fun compare(expected: PsiElement, actual: PsiElement): TreeComparingResult {
        return when (expected) {
            is LeafPsiElement -> compareLeafElements(expected, actual)
            is KtFile -> compareKtFiles(expected, actual)
            is KtNamedFunction -> compareFunctions(expected, actual)
            else -> compareDefault(expected, actual)
        }
    }

    private fun compareLeafElements(
        expected: LeafPsiElement,
        actual: PsiElement
    ): TreeComparingResult {
        return if (actual is LeafPsiElement && expected.text == actual.text) {
            Same
        } else {
            Different("Different leaves element", listOf()).wrapIn(
                DifferenceStackElement(
                    "While comparing leaves elements",
                    expected.getPosition(expectedDocument),
                    actual.getPosition(actualDocument)
                )
            )
        }
    }

    private fun compareKtFiles(expected: PsiElement, actual: PsiElement): TreeComparingResult {
        return if (actual !is KtFile) {
            Different("Actual is not KtFile")
        } else {
            val expectedChildren = expected.childrenNoWhitespaces.filter { it !is KtImportList }
            val actualChildren = actual.childrenNoWhitespaces.filter { it !is KtImportList }
            compareChildren(expectedChildren, actualChildren)
        }.wrapIn(
            DifferenceStackElement(
                "While comparing KtFiles",
                expected.getPosition(expectedDocument),
                actual.getPosition(actualDocument)
            )
        )
    }

    private fun compareChildren(
        expectedChildren: List<PsiElement>,
        actualChildren: List<PsiElement>
    ): TreeComparingResult {
        return if (expectedChildren.size != actualChildren.size)
            Different("Different number of children")
        else
            expectedChildren.zip(actualChildren).map { (expectedChild, actualChild) ->
                compare(expectedChild, actualChild)
            }.firstIsInstanceOrNull<Different>() ?: Same
    }

    private fun compareFunctions(
        expected: KtNamedFunction,
        actual: PsiElement
    ): TreeComparingResult {
        return if (actual !is KtNamedFunction) {
            Different("Other is not KtNamedFunction")
        } else {
            val expectedChildren = expected.childrenNoWhitespaces.filter { element ->
                element !is KtModifierList
            }.trimUnitReturnType()
            val actualChildren = actual.childrenNoWhitespaces.filter { element ->
                element !is KtModifierList
            }.trimUnitReturnType()

            val compareModifierLists = compareModifierLists(expected, actual)

            if (compareModifierLists is Different) {
                compareModifierLists
            } else {
                compareChildren(expectedChildren, actualChildren)
            }
        }.wrapIn(
            DifferenceStackElement(
                "When comparing KtNamedFunctions",
                expected.getPosition(expectedDocument),
                actual.getPosition(actualDocument)
            )
        )
    }

    private fun compareModifierLists(
        expected: KtNamedFunction,
        actual: PsiElement
    ): TreeComparingResult {
        val expectedKtModifierListChildren =
            expected.childrenNoWhitespaces.firstIsInstanceOrNull<KtModifierList>()
                ?.childrenNoWhitespaces
                ?.filter { !(it is LeafPsiElement && it.text == "public") }
                ?: emptyList()


        val actualKtModifierListChildren =
            actual.childrenNoWhitespaces.firstIsInstanceOrNull<KtModifierList>()
                ?.childrenNoWhitespaces
                ?.filter { !(it is LeafPsiElement && it.text == "public") }
                ?: emptyList()

        return compareChildren(
            expectedKtModifierListChildren,
            actualKtModifierListChildren
        )
    }

    private fun compareDefault(expected: PsiElement, actual: PsiElement): TreeComparingResult {
        return compareChildren(expected.childrenNoWhitespaces, actual.childrenNoWhitespaces).wrapIn(
            DifferenceStackElement(
                "While comparing PsiElements",
                expected.getPosition(expectedDocument),
                actual.getPosition(actualDocument)
            )
        )
    }
}

val badFollowingOfColonMessage = """
    Bad grammar: colon in fun-declaration should be followed by KtTypeReference.
""".trimIndent()

private fun List<PsiElement>.trimUnitReturnType(): List<PsiElement> {
    val result = mutableListOf<PsiElement>()
    val currentIterator = iterator()
    while (currentIterator.hasNext()) {
        val current = currentIterator.next()
        if (current.node.elementType != KtTokens.COLON) {
            result.add(current)
        } else {
            val following = currentIterator.next()
            require(following is KtTypeReference) { badFollowingOfColonMessage }
            if (following.text == "Unit") {
                continue
            } else {
                result.add(current)
                result.add(following)
            }
        }
    }
    return result
}
