import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import extractEqualityParts
import org.junit.Assert
import org.junit.Test

class ExtractEqualityTest {
    private fun <U, V> Result<U, V>.isError() = this is Err<V>

    @Test
    fun testEquality() {
        Assert.assertEquals(
            Ok("sum(30, 50)" to "80"),
            extractEqualityParts("sum(30, 50)== 80")
        )
        Assert.assertEquals(
            Ok("sum(30, 50)" to "80"),
            extractEqualityParts("sum(30, 50) == 80")
        )
        Assert.assertEquals(
            Ok("sum(30, 50)" to "80"),
            extractEqualityParts("sum(30, 50) ==80")
        )
    }

    @Test
    fun testWeirdEquality() {
        Assert.assertEquals(
            Ok("sum(30, 50 == 50)" to "80"),
            extractEqualityParts("sum(30, 50 == 50)== 80")
        )
    }

    @Test
    fun badEqualities() {
        Assert.assertTrue(extractEqualityParts("sum(20, 30)").isError())
        Assert.assertTrue(extractEqualityParts("sum(20, 30) == ").isError())
        Assert.assertTrue(extractEqualityParts("sum(20, 30) == 5)").isError())
        Assert.assertTrue(extractEqualityParts("sum(20, 30) - 5").isError())
    }
}