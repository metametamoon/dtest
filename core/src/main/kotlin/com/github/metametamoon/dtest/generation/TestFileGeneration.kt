package com.github.metametamoon.dtest.generation

import com.github.metametamoon.dtest.TestUnit
import com.github.metametamoon.dtest.util.DtestSettings
import com.github.metametamoon.dtest.util.Imports
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

object GenerationUtils {
    fun getClassNameForNamedObject(name: String): String = "$name tests"
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
        val className = GenerationUtils.getClassNameForNamedObject(testUnit.testedObjectName)
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
    // the library uses \n as a divider, see [com.squareup.kotlinpoet.FileSpec.emit] as an example
    val lines = code.split("\n")
    val pathSegments = packageFqName.pathSegments()
    val fileFqName =
        if (pathSegments.size > 0)
            FqName.fromSegments(pathSegments.map { "$it" } + fileName)
        else
            FqName(fileName)
    val linesWithGoodImports = addStrictImportsIfNecessary(lines, settings.imports[fileFqName.asString()])
    return linesWithGoodImports
}

fun addStrictImportsIfNecessary(lines: List<String>, imports: Imports?): List<String> {
    return if (imports == null || !imports.strict) {
        lines
    } else {
        val linesWithFilteredImports = lines.filter { !it.startsWith("import ") }
        val importLines = createStrictImportLines(imports.importEntries)
        if (linesWithFilteredImports.firstOrNull()?.startsWith("package") == true) {
            val prefix = listOf(linesWithFilteredImports.first(), "")
            val postFix = listOf("") + linesWithFilteredImports.subList(1, linesWithFilteredImports.size)
            prefix + importLines + postFix
        } else {
            importLines + "" + linesWithFilteredImports
        }
    }
}

fun createStrictImportLines(importEntries: List<String>): List<String> =
    importEntries.map { "import $it" }

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
    // if the imports are strict, we should not spend time here to add entries as they will be hard-replaced later anyway
    val imports = settings.imports[fileFqName.asString()]?.importEntries.orEmpty().map(::FqName) + testAnnotationFqName
    val unimportantName = "A"
    var file = FileSpec.builder(packageFqName.asString(), unimportantName)
    for (import in imports)
        file = file.addImport(import.parent().asString(), import.shortName().asString())
    for (type in classes) {
        file = file.addType(type)
    }
    return file
}