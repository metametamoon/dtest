package tree.comparer

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.FqName
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
    fun compare(expected: PsiElement, actual: PsiElement): TreeComparingResult {
        return when (expected) {
            is LeafPsiElement -> compareLeafElements(expected, actual)
            is KtFile -> compareKtFiles(expected, actual)
            is KtNamedFunction -> compareFunctions(expected, actual)
            is KtImportList -> compareImportLists(expected, actual)
            else -> compareDefault(expected, actual)
        }
    }

    private fun List<FqName>.excludeKotlinDefaultInclusion(): List<FqName> = filterNot {
        val segments = it.pathSegments()
        segments.size == 2 && segments.first().asString() == "kotlin"
    }

    private fun compareImportLists(expected: KtImportList, actual: PsiElement): TreeComparingResult {
        return (if (actual !is KtImportList) {
            Different("Actual is not KtImportList")
        } else {
            val expectedImports = expected.imports.map {
                it.importedFqName
                    ?: throw IllegalArgumentException("Not an import in import list, probably parsing error")
            }.excludeKotlinDefaultInclusion().map { it.asString() }.toSet()

            val actualImports = actual.imports.map {
                it.importedFqName
                    ?: throw IllegalArgumentException("Not an import in import list, probably parsing error")
            }.excludeKotlinDefaultInclusion().map { it.asString() }.toSet()
            if (expectedImports != actualImports) {
                Different("Imports are not identical (ignoring order)")
            } else {
                Same
            }
        }).wrapIn(DifferenceStackElement("While comparing KtImportLists", expected.textOffset, actual.textOffset))
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
                    expected.textOffset,
                    actual.textOffset
                )
            )
        }
    }

    private fun compareKtFiles(expected: PsiElement, actual: PsiElement): TreeComparingResult {
        return if (actual !is KtFile) {
            Different("Actual is not KtFile")
        } else {
            val expectedChildren = expected.childrenNoWhitespaces
            val actualChildren = actual.childrenNoWhitespaces
            compareChildren(expectedChildren, actualChildren)
        }.wrapIn(
            DifferenceStackElement(
                "While comparing KtFiles",
                expected.textOffset,
                actual.textOffset
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
                expected.textOffset,
                actual.textOffset
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
                expected.textOffset,
                actual.textOffset
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
