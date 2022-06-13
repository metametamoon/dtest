package generation

import TestInfo
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.name.FqName
import org.junit.jupiter.api.Test

fun generateTestFile(
    testInfos: List<TestInfo>,
    packageFqName: FqName,
    baseClassFqName: FqName? = null
): List<String> {
    val classes = testInfos.map { testInfo ->
        TypeSpec.Companion.classBuilder(testInfo.name + " tests")
            .addFunction(
                FunSpec.builder("1").addAnnotation(
                    Test::class
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
    var file = FileSpec.builder(packageFqName.asString(), "A")
        .addImport("org.junit.jupiter.api", "Assertions")
    for (type in classes) {
        file = file.addType(type)
    }

    val code = file.build().toString()
    return code.split("\n")
}