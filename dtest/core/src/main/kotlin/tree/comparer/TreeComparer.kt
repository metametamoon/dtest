package tree.comparer

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

class TreeComparer {
    fun compare(expected: PsiElement, actual: PsiElement): Boolean {
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
    ) = actual is LeafPsiElement && expected.text == actual.text

    private fun compareKtFiles(expected: PsiElement, actual: PsiElement) =
        if (actual !is KtFile)
            false
        else {
            val expectedChildren = expected.childrenNoWhitespaces.filter { it !is KtImportList }
            val actualChildren = actual.childrenNoWhitespaces.filter { it !is KtImportList }
            compareChildren(expectedChildren, actualChildren)
        }

    private fun compareChildren(
        expectedChildren: List<PsiElement>,
        actualChildren: List<PsiElement>
    ): Boolean {
        return if (expectedChildren.size != actualChildren.size)
            false
        else
            expectedChildren.zip(actualChildren).all { (expectedChild, actualChild) ->
                compare(expectedChild, actualChild)
            }
    }

    private fun compareFunctions(
        expected: KtNamedFunction,
        actual: PsiElement
    ): Boolean {
        return if (actual !is KtNamedFunction) {
            false
        } else {
            val expectedChildren = expected.childrenNoWhitespaces.filter { element ->
                element !is KtModifierList
            }.trimUnitReturnType()
            val expectedKtModifierListChildren =
                expected.childrenNoWhitespaces.firstIsInstanceOrNull<KtModifierList>()
                    ?.childrenNoWhitespaces
                    ?.filter { !(it is LeafPsiElement && it.text == "public") }
                    ?: emptyList()

            val actualChildren = actual.childrenNoWhitespaces.filter { element ->
                element !is KtModifierList
            }.trimUnitReturnType()

            val actualKtModifierListChildren =
                actual.childrenNoWhitespaces.firstIsInstanceOrNull<KtModifierList>()
                    ?.childrenNoWhitespaces
                    ?.filter { !(it is LeafPsiElement && it.text == "public") }
                    ?: emptyList()

            return compareModifierListChildren(
                expectedKtModifierListChildren,
                actualKtModifierListChildren
            ) && compareChildren(expectedChildren, actualChildren)
        }
    }

    private fun compareModifierListChildren(
        expectedKtModifierListChildren: List<PsiElement>?,
        actualKtModifierListChildren: List<PsiElement>?
    ): Boolean {
        return when {
            expectedKtModifierListChildren == null && actualKtModifierListChildren == null ->
                true
            expectedKtModifierListChildren != null && actualKtModifierListChildren != null ->
                compareChildren(expectedKtModifierListChildren, actualKtModifierListChildren)
            else -> false
        }
    }

    private fun compareDefault(expected: PsiElement, actual: PsiElement) =
        if (expected.childrenNoWhitespaces.size != actual.childrenNoWhitespaces.size)
            false
        else {
            expected.childrenNoWhitespaces.zip(actual.childrenNoWhitespaces).all { (expectedChild, actualChild) ->
                compare(expectedChild, actualChild)
            }
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
