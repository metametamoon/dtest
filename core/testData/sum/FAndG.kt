import kotlin.Unit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private class `f tests` {
    @Test
    public fun `1`(): Unit {
        Assertions.assertEquals(42, f())
    }
}

private class `g tests` {
    @Test
    public fun `1`(): Unit {
        Assertions.assertEquals(-42, g())
    }
}