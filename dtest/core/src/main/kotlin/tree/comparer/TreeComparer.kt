package tree.comparer

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtNamedFunction

class TreeComparer {
    fun compare(expected: PsiElement, actual: PsiElement): Boolean {
        return when (expected) {
            is LeafPsiElement -> {
                actual is LeafPsiElement && expected.text == actual.text
            }
            is KtNamedFunction -> {
                if (actual !is KtNamedFunction)
                    return false
                else {
                    if (actual.firstChild is KtModifierList) {
                        if (actual.firstChild.firstChild.text != "public") {
                            return false
                        } else {
                            val actualChildrenWithoutModifiers = actual.children.drop(1)
                            return expected.children.zip(actualChildrenWithoutModifiers)
                                .all { (expectedChild, actualChild) ->
                                    compare(expectedChild, actualChild)
                                }
                        }
                    } else return compareDefault(expected, actual)
                }
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