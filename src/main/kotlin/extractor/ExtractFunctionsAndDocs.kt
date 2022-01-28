package extractor

import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitor

fun extractFunctionsAndDocs(ktFile: KtFile): MutableList<Pair<KDoc, KtNamedFunction>> {
    val list = mutableListOf<Pair<KDoc, KtNamedFunction>>()
    ktFile.accept(object : KtTreeVisitor<Unit>() {
        override fun visitNamedFunction(
            function: KtNamedFunction,
            data: Unit?
        ): Void? {
            function.docComment?.also { kDoc ->
                list.add(kDoc to function)
            }
            return super.visitNamedFunction(function, data)
        }
    })
    return list
}