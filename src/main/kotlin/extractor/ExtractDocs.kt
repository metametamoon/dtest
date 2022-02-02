package extractor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiManager
import com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtTreeVisitor
import java.io.File
import java.io.IOException

internal val globalKotlinParserOnlyProject by lazy {
    val configuration = CompilerConfiguration()
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE
    )

    KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
}

fun extractDocs(path: String): Result<ExtractedDocs, Exception> {
    val file = File(path)
    val text = try {
        file.readText().replace("\r\n", "\n")
    } catch (e: IOException) {
        return Err(e)
    }
    val currentFile =
        createKtFile(text, file.absolutePath, globalKotlinParserOnlyProject)
    val documentations = mutableListOf<Pair<KtElement, KDoc>>()
    currentFile.accept(object : KtTreeVisitor<Unit>() {
        override fun visitDeclaration(dcl: KtDeclaration, data: Unit?): Void? {
            dcl.docComment?.also { kDoc ->
                documentations.add(dcl to kDoc)
            }
            return super.visitDeclaration(dcl, data)
        }
    })
    return Ok(ExtractedDocs(documentations))
}

private fun createKtFile(
    codeString: String, fileName: String, project: Project
) = PsiManager.getInstance(project).findFile(
    LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
) as KtFile

