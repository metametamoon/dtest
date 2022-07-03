package tree.comparer

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTypeReference

val PsiElement.childrenNoWhitespaces: List<PsiElement>
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
            is LeafPsiElement -> {
                actual is LeafPsiElement && expected.text == actual.text
            }
            is KtNamedFunction -> compareFunctions(expected, actual)

            else -> {
                compareDefault(expected, actual)
            }
        }
    }

    private fun compareFunctions(
        expected: KtNamedFunction,
        actual: PsiElement
    ): Boolean {
        return if (actual !is KtNamedFunction)
            false
        else {
            val expectedChildren = expected.childrenNoWhitespaces.filter { element ->
                !(element is KtModifierList && element.firstChild.text == "public")
            }.trimUnitReturnType()

            val actualChildren = actual.childrenNoWhitespaces.filter { element ->
                !(element is KtModifierList && element.firstChild.text == "public")
            }.trimUnitReturnType()

            return expectedChildren.zip(actualChildren).all { (expectedChild, actualChild) ->
                compare(expectedChild, actualChild)
            }
        }
    }

    private fun compareDefault(expected: PsiElement, actual: PsiElement) =
        if (expected.childrenNoWhitespaces.size != actual.childrenNoWhitespaces.size)
            false
        else {
            val all =
                expected.childrenNoWhitespaces.zip(actual.childrenNoWhitespaces).all { (expectedChild, actualChild) ->
                    compare(expectedChild, actualChild)
                }
            all
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
            if (following.text == "Unit")
                continue
            else {
                result.add(current)
                result.add(following)
            }
        }
    }
    return result
}
