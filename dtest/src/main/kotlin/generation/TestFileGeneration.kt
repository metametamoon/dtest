package generation

import TestInfo
import com.squareup.kotlinpoet.*
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
        .addImport("org.junit.jupiter.api", "Assertions")
    for (type in classes) {
        file = file.addType(type)
    }
    file = file.addFunction(
        FunSpec.builder("shouldBe")
            .addModifiers(KModifier.PRIVATE, KModifier.INFIX)
            .receiver(ClassName("kotlin", "Any"))
            .addParameter("expected", ClassName("kotlin", "Any"))
            .addCode("Assertions.assertEquals(expected, this)")
            .build()
    )

    val code = file.build().toString()
    return code.split("\n")
}