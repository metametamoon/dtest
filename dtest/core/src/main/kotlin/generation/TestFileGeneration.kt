package generation

import TestInfo
import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.name.FqName

fun generateTestFile(
    testInfos: List<TestInfo>,
    packageFqName: FqName,
    defaultTestAnnotationFqName: String,
    baseClassFqName: FqName? = null,
): List<String> {
    val testAnnotationFqName = FqName(defaultTestAnnotationFqName)
    val classes = testInfos.map { testInfo ->
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
    var file = FileSpec.builder(packageFqName.asString(), "A")
        .addImport("org.junit.jupiter.api", "Assertions")
    for (type in classes) {
        file = file.addType(type)
    }

    val code = file.build().toString()
    return code.split("\n")
}