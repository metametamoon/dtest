import kotlin.test.Test

public class `foo tests` {
    @Test
    fun `0`() {
        true != false
    }

    @Test
    fun `1`() {
        5 < 6
    }
}

public class `bar tests` {
    @Test
    fun `0`() {
        println("Status: clear")
    }
}