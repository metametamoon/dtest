package generation

import TestInfo
import com.squareup.kotlinpoet.*
import docs_to_tests.snippets.CodeSnippet
import org.jetbrains.kotlin.name.FqName

fun generateTestFile(
    testInfos: List<TestInfo>,
    packageFqName: FqName,
    defaultTestAnnotationFqName: String,
    baseClassFqName: FqName? = null,
): List<String> {
    val testAnnotationFqName = FqName(defaultTestAnnotationFqName)
    val classes = testInfos.map { testInfo ->
        print(testInfo)
        TypeSpec.Companion.classBuilder(testInfo.name + " tests")
            .addFunction(
                FunSpec.builder("1").addAnnotation(
                    ClassName(testAnnotationFqName.parent().asString(), testAnnotationFqName.shortName().asString())
                ).addCode(testInfo.snippets.first().snippet).build()
            )
            .addModifiers(KModifier.PRIVATE)
            .let {
                if (baseClassFqName == null)
                    it
                else it.superclass(
                    ClassName(
                        baseClassFqName.parent().asString(),
                        baseClassFqName.shortName().asString()
                    )
                )
            }
            .build()
    }
    val file = addImportsAndTypes(packageFqName, classes, testInfos, testAnnotationFqName)

    val code = file.build().toString()
    return code.split("\n")
}

private fun addImportsAndTypes(
    packageFqName: FqName,
    classes: List<TypeSpec>,
    testInfos: List<TestInfo>,
    testAnnotationFqName: FqName
): FileSpec.Builder {
    val imports = testInfos.flatMap(TestInfo::snippets)
        .mapNotNull(CodeSnippet::importsSnippet)
        .flatMap { importSnippet ->
            val importRegex = "import (?<import>.*)".toRegex()
            importRegex.findAll(importSnippet).map { match -> match.groups["import"]?.value }
        }
        .filterNotNull()
        .map(::FqName) + listOf(testAnnotationFqName)

    var file = FileSpec.builder(packageFqName.asString(), "A")
    for (import in imports)
        file = file.addImport(import.parent().asString(), import.shortName().asString())
    for (type in classes) {
        file = file.addType(type)
    }
    return file
}