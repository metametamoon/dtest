import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ExtractEqualityTest {
    private fun <U, V> Result<U, V>.isError() = this is Err<V>

    @Test
    fun testEquality() {
        Assertions.assertEquals(Ok("sum(30, 50)" to "80"), extractEqualityParts("sum(30, 50)== 80"))
        Assertions.assertEquals(Ok("sum(30, 50)" to "80"), extractEqualityParts("sum(30, 50) == 80"))
        Assertions.assertEquals(Ok("sum(30, 50)" to "80"), extractEqualityParts("sum(30, 50) ==80"))
    }

    @Test
    fun testWeirdEquality() {
        Assertions.assertEquals(Ok("sum(30, 50 == 50)" to "80"), extractEqualityParts("sum(30, 50 == 50)== 80"))
    }

    @Test
    fun badEqualities() {
        Assertions.assertTrue(extractEqualityParts("sum(20, 30)").isError())
        Assertions.assertTrue(extractEqualityParts("sum(20, 30) == ").isError())
        Assertions.assertTrue(extractEqualityParts("sum(20, 30) == 5)").isError())
        Assertions.assertTrue(extractEqualityParts("sum(20, 30) - 5").isError())
    }
}