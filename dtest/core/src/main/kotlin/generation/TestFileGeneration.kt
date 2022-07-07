package generation

import TestUnit
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.name.FqName
import util.DtestSettings

private fun TypeSpec.Builder.addFunctionsFromSnippets(
    testAnnotationFqName: FqName,
    testUnit: TestUnit
): TypeSpec.Builder {
    val unitReturnType = protectedFromShadowingKotlinUnitType()
    var builder = this
    for ((index, snippet) in testUnit.testSnippets.withIndex()) {
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
    testUnits: List<TestUnit>,
    packageFqName: FqName,
    settings: DtestSettings,
    baseClassFqName: FqName? = null,
    fileName: String,
): List<String> {
    val defaultTestAnnotationFqName = settings.defaultTestAnnotationFqName
    val testAnnotationFqName = FqName(defaultTestAnnotationFqName)
    val classes = testUnits.map { testUnit ->
        TypeSpec.Companion.classBuilder("${testUnit.testedObjectName} tests")
            .addFunctionsFromSnippets(testAnnotationFqName, testUnit)
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
    val file = addImportsAndTypes(
        packageFqName,
        classes,
        testAnnotationFqName,
        settings,
        fileName
    )

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
    testAnnotationFqName: FqName,
    settings: DtestSettings,
    fileName: String
): FileSpec.Builder {
    val fileFqName = FqName.fromSegments(
        packageFqName.pathSegments().map { "$it" } + fileName
    )
    val imports = settings.imports[fileFqName.asString()].orEmpty().map(::FqName) + testAnnotationFqName
    var file = FileSpec.builder(packageFqName.asString(), "A")
    for (import in imports)
        file = file.addImport(import.parent().asString(), import.shortName().asString())
    for (type in classes) {
        file = file.addType(type)
    }
    return file
}