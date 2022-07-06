package functional

//import tree.comparer.TreeComparer
//import java.io.File
//import kotlin.io.path.createTempDirectory
//import kotlin.io.path.relativeTo

//class FunctionalTests {
//    private val kotlinParserProject = run {
//        val configuration = CompilerConfiguration()
//        configuration.put(
//            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE
//        )
//        KotlinCoreEnvironment.createForProduction(
//            Disposer.newDisposable(),
//            configuration,
//            EnvironmentConfigFiles.JVM_CONFIG_FILES
//        ).project
//    }
//
//    private fun createKtFile(
//        file: File
//    ): KtFile {
//        val codeString = file.readLines().joinToString("\n")
//        val fileName = file.name
//        return PsiManager.getInstance(kotlinParserProject).findFile(
//            LightVirtualFile(fileName, KotlinFileType.INSTANCE, codeString)
//        ) as KtFile
//    }
//
//    @TestFactory
//    fun createTestsFromFolder(): List<DynamicTest> {
//        return File("functional-tests").listFiles().filterNotNull().map { file ->
//            generateFunctionalTest(file)
//        }
//    }
//
//    private val treeComparer = TreeComparer(expectedDocument, actualDocument)
//
//    private fun generateFunctionalTest(testFolder: File): DynamicTest {
//        val facade = DtestFileGenerator("kotlin.test.Test")
//        return DynamicTest.dynamicTest(testFolder.name) {
//            val filesWithKtExtension = testFolder.resolve("kotlin-src").walkBottomUp()
//                .filter { it.isFile }
//                .filter { it.extension == "kt" }
//                .toList()
//            val genDirectory = createTempDirectory("").toFile()
//            filesWithKtExtension.forEach { file ->
//                facade.generateTests(file, genDirectory)
//            }
//            val expectedFilesDirectory = testFolder.resolve("expected-gen")
//            val expectedFiles = expectedFilesDirectory.walkBottomUp().filter { it.isFile }.toList()
//            val relativePathsOfExpectedFiles =
//                expectedFiles.associateWith { it.toPath().relativeTo(expectedFilesDirectory.toPath()) }
//            val expectedToGeneratedMapping =
//                relativePathsOfExpectedFiles.toList().associate { (generatedFile, relativePath) ->
//                    generatedFile to genDirectory.toPath().resolve(relativePath).toFile()
//                }
//            for ((expectedFile, generatedFile) in expectedToGeneratedMapping) {
//                val relativePath = expectedFile.relativeTo(expectedFilesDirectory).toPath()
//                if (!generatedFile.exists() || !generatedFile.isFile) {
//                    throw IllegalArgumentException("Expected file $relativePath to be generated, but it was not.")
//                } else {
//                    if (treeComparer.compare(createKtFile(expectedFile), createKtFile(generatedFile)).areSame()) {
//                        throw IllegalArgumentException("File $relativePath was improperly generated.")
//                    }
//                }
//            }
//            if (expectedFiles.size > filesWithKtExtension.size) {
//                throw IllegalArgumentException("There are ${expectedFiles.size - filesWithKtExtension.size} excessive files.")
//            }
//        }
//    }
//}