import extractor.createKtFile
import extractor.createNewProject
import extractor.extractEqualityParts
import extractor.extractFunctionsAndDocs
import java.io.File

fun main() {
    val project = createNewProject()
    val file = createKtFile(
        File("src/main/kotlin/Sum.kt").readText().replace("\r\n", "\n"),
        "Sum.kt",
        project
    )
    extractEqualityParts("sum(30, 4) == 34")
    println(extractFunctionsAndDocs(file))
}