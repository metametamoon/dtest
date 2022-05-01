package hello.packages

/**
 * ```Assertions.assertEquals(7, ok(3,4))```
 * ```Assertions.assertEquals(7, ok(4, 3))```
 */
fun ok(a: Int, b: Int): Int {
    return a + b
}