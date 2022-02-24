import kotlin.Unit
import org.junit.jupiter.api.Test
import util.shouldBe

private class `f tests` {
    @Test
    fun `1`() {
        f() shouldBe 42
    }
}

private class `g tests` {
    @Test
    fun `1`() {
        g() shouldBe -42
    }
}