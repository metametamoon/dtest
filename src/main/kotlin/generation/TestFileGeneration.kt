package generation

import TestInfo
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.jetbrains.kotlin.name.FqName
import org.junit.jupiter.api.Test

fun generateTestFile(
    testInfos: List<TestInfo>,
    packageFqName: FqName
): List<String> {
    val classes = testInfos.map { testInfo ->
        TypeSpec.Companion.classBuilder(testInfo.name + " tests")
            .addFunction(
                FunSpec.builder("1").addAnnotation(
                    Test::class
                ).addCode(testInfo.snippets.first().snippet).build()
            )
            .addModifiers(KModifier.PRIVATE)
            .build()
    }
    var file = FileSpec.builder(packageFqName.asString(), "A")
        .addImport("util", "shouldBe")
    for (type in classes) {
        file = file.addType(type)
    }

    val code = file.build().toString()
    return code.split("\n")
}