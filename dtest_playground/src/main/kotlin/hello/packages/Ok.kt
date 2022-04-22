package hello.packages

/**
 * ```ok(3, 4) shouldBe 7```
 * ```ok(4, 3) shouldBe 7```
 */
fun ok(a: Int, b: Int): Int {
    return a + b
}