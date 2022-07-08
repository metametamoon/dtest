package com.github.metametamoon.extraction

import com.intellij.psi.PsiComment
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/**
 * Looks for the base test class setting in the comments of [ktFile].
 * The current setting format is `dtestBaseClass="com.example.SampleBase"`.
 */
fun extractBaseTestClass(ktFile: KtFile): FqName? {
    var baseTestClassFqName: FqName? = null
    val visitor = object : KtTreeVisitorVoid() {
        override fun visitComment(comment: PsiComment) {
            super.visitComment(comment)
            val pattern = "dtestBaseClass=\"(.*?)\"".toRegex()
            val baseTestClassMatchOrNull = pattern.find(comment.text)
            baseTestClassMatchOrNull?.let { match ->
                val baseClassName = match.groups[1] ?: throw InternalError("No matching group in pattern $pattern")
                baseTestClassFqName = FqName(baseClassName.value)
            }
        }
    }
    ktFile.accept(visitor)
    return baseTestClassFqName
}