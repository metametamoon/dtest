import com.intellij.lang.ASTNode
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
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.children
import org.junit.jupiter.api.Test
import java.io.File

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

class GeneratorTest {
    // use DebugUtil.psiToString()
    private fun createKtFile(
        file: File, project: Project
    ): KtFile {
        val codeString = file.readLines().joinToString("\n")
        val fileName = file.name
        return PsiManager.getInstance(project).findFile(
            LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
        ) as KtFile
    }

    @Test
    fun `contains test code`() {
        val ktFile = createKtFile(
            File("testData/sum/TestSum.kt"), globalKotlinParserOnlyProject
        )
        val a: ASTNode = ktFile.node
        visit(a, 0)
//        println(DebugUtil.psiToString(ktFile, true, false))
    }

    private fun visit(node: ASTNode, depth: Int) {
        //        if (a is LeafElement && a.elementType.debugName != "WHITE_SPACE") {
//            println(a.elementType.debugName)
//        }
        fun isWhitespaceNode() = node.elementType.debugName == "WHITE_SPACE"
        fun flatText() = node.text.replace("\n", "; ")
        fun indentationAndName() =
            ("  ".repeat(depth) + " " + node.elementType.debugName)
        if (!isWhitespaceNode()) println(
            indentationAndName().padEnd(50, ' ') + flatText()
        )
        for (child in node.children()) visit(child, depth + 1)
    }
}
