package com.github.metametamoon.dtest.generation

import com.github.metametamoon.dtest.TestUnit
import com.github.metametamoon.dtest.util.DtestSettings
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private fun TypeSpec.Builder.addFunctionsFromSnippets(
    testAnnotationFqName: FqName,
    testUnit: TestUnit
): TypeSpec.Builder {
    val unitReturnType = protectedFromShadowingKotlinUnitType()
    var builder = this
    for ((index, snippet) in testUnit.testSnippets.withIndex()) {
        builder = builder.addFunction(
            FunSpec.builder(index.toString()).addAnnotation(
                testAnnotationFqName.toClassName()
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
        val className = "${testUnit.testedObjectName} tests"
        TypeSpec.Companion.classBuilder(className)
            .addSuperclassFromSettings(settings, packageFqName, className)
            .addFunctionsFromSnippets(testAnnotationFqName, testUnit)
            .addModifiers(KModifier.PUBLIC)
            .let {
                if (baseClassFqName == null)
                    it
                else it.superclass(
                    baseClassFqName.toClassName()
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

private fun TypeSpec.Builder.addSuperclassFromSettings(
    settings: DtestSettings,
    packageFqName: FqName,
    className: String
): TypeSpec.Builder {
    var fqLookupName = packageFqName.child(Name.identifier(className))
    while (fqLookupName.pathSegments().size > 0) {
        val possibleParent = settings.classParents[fqLookupName.asString()]
        if (possibleParent != null)
            return this.superclass(FqName(possibleParent).toClassName())
        else
            fqLookupName = fqLookupName.parent()
    }
    return this
}

private fun FqName.toClassName() =
    ClassName(
        parent().asString(),
        shortName().asString()
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