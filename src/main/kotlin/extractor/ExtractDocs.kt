package extractor

import DocsExtract
import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtTreeVisitor
import java.io.File

val globalParserOnlyKotlinProject by lazy {
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

fun extractDocs(path: String): DocsExtract {
    val file = File(path)
    val text = file.readText().replace("\r\n", "\n")
    val currentFile =
        createKtFile(text, file.absolutePath, globalParserOnlyKotlinProject)
    val documentations = mutableListOf<Pair<KtElement, KDoc>>()
    currentFile.accept(object : KtTreeVisitor<Unit>() {
        override fun visitDeclaration(dcl: KtDeclaration, data: Unit?): Void? {
            dcl.docComment?.also { kDoc ->
                documentations.add(dcl to kDoc)
            }
            return super.visitDeclaration(dcl, data)
        }
    })
    return DocsExtract(currentFile, documentations)
}
