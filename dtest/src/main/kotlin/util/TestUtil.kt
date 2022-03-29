package util

import org.junit.jupiter.api.Assertions

@Suppress("unused") // part of public api
infix fun Any?.shouldBe(expected: Any) {
    Assertions.assertEquals(expected, this)
}