package tree.comparer

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement

class TreeComparer {
    fun compare(expected: PsiElement, actual: PsiElement): Boolean {
        return when (expected) {
            is LeafPsiElement -> {
                actual is LeafPsiElement && expected.text == actual.text
            }
            else -> {
                compareDefault(expected, actual)
            }
        }
    }

    private fun compareDefault(expected: PsiElement, actual: PsiElement) =
        if (expected.children.size != actual.children.size)
            false
        else expected.children.zip(actual.children).all { (expectedChild, actualChild) ->
            compare(expectedChild, actualChild)
        }
}