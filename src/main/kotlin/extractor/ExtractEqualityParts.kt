package extractor

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
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
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtProperty


fun extractEqualityParts(equality: String): Result<EqualityParts, String> {
    val project = createNewProject()
    // TODO: fix this workaround (the parser doesn't like it if there is only one expression
    //  in file, so the dummy property declaration is added).
    val ktFile = createKtFile("val i = $equality", "sum.kt", project)
    ktFile.children.forEach {
        if (it is KtProperty) {
            return@extractEqualityParts extractEqualityPartsFromKtProperty(it)
        }
    }
    return Err("Failed")
}

fun createNewProject(): Project {
    val configuration = CompilerConfiguration()
    configuration.put(
        CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
        MessageCollector.NONE
    )

    return KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        configuration,
        EnvironmentConfigFiles.JVM_CONFIG_FILES
    ).project
}

private fun extractBinaryExpression(property: KtProperty): Result<KtBinaryExpression, String> {
    val binaryExpression = property.lastChild as? KtBinaryExpression
        ?: return Err("Not a binary expression")
    val operator = binaryExpression.operationToken
    if (operator.debugName != "EQEQ") return Err("Not an equality")
    return Ok(binaryExpression)
}

data class EqualityParts(
    val actualExpression: String,
    val expectedExpression: String
)

@Suppress("RemoveExplicitTypeArguments")
private fun extractEqualityPartsFromKtProperty(property: KtProperty) =
    binding<EqualityParts, String> {
        val equalityExpression = extractBinaryExpression(property).bind()
        val left = equalityExpression.left?.text
            ?: Err("Left part opf expression is void").bind<Nothing>()
        val right = equalityExpression.right?.text
            ?: Err("Left part opf expression is void").bind<Nothing>()
        EqualityParts(left, right)
    }

fun createKtFile(codeString: String, fileName: String, project: Project) =
    PsiManager.getInstance(project).findFile(
        LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
    ) as KtFile