package generation

import TestInfo
import com.squareup.kotlinpoet.*
import docs_to_tests.snippets.CodeSnippet
import org.jetbrains.kotlin.name.FqName

private fun TypeSpec.Builder.addFunctionsFromSnippets(
    testAnnotationFqName: FqName,
    testInfo: TestInfo
): TypeSpec.Builder {
    val unitReturnType = protectedFromShadowingKotlinUnitType()
    var builder = this
    for ((index, snippet) in testInfo.snippets.withIndex()) {
        builder = builder.addFunction(
            FunSpec.builder(index.toString()).addAnnotation(
                getClassNameFromFqName(testAnnotationFqName)
            ).addCode(snippet.snippet)
                .returns(unitReturnType).build()
        )
    }
    return builder
}

private fun protectedFromShadowingKotlinUnitType(): ClassName {
    val l = "l"
    return ClassName("kot" + "${l}in", "Unit")
}

fun generateTestFile(
    testInfos: List<TestInfo>,
    packageFqName: FqName,
    defaultTestAnnotationFqName: String,
    baseClassFqName: FqName? = null,
): List<String> {
    val testAnnotationFqName = FqName(defaultTestAnnotationFqName)
    val classes = testInfos.mapIndexed { index, testInfo ->
        TypeSpec.Companion.classBuilder("${testInfo.name} tests")
            .addFunctionsFromSnippets(testAnnotationFqName, testInfo)
            .addModifiers(KModifier.PUBLIC)
            .let {
                if (baseClassFqName == null)
                    it
                else it.superclass(
                    getClassNameFromFqName(baseClassFqName)
                )
            }
            .build()
    }
    val file = addImportsAndTypes(packageFqName, classes, testInfos, testAnnotationFqName)

    val code = file.build().toString()
    return code.split("\n")
}

private fun getClassNameFromFqName(testAnnotationFqName: FqName) =
    ClassName(
        testAnnotationFqName.parent().asString(),
        testAnnotationFqName.shortName().asString()
    )

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