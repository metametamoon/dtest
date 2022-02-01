@file:Suppress("unused")

/**
 * This functions sums two integer numbers.
 *  >>> assertEquals(11, sum(5, 6))
 */
fun sum(a: Int, b: Int) = a + b

/**
 * Meh, adds one two the sum
 *  >>> assertEquals(12, sum1(5, 6))
 *  >>> assertEquals(1, sum1(0, 0))
 */
fun sum1(a: Int, b: Int) = a + b + 1

class Const {
    companion object {
        /**
         *  >>> assertEquals(42, Const.constant())
         */
        fun constant() = 43
    }
}

/**
 * >>> f()
 */
fun f() {
    g()
}

fun g() {
    h()
}

fun h() {
    p()
}

fun p() {
    throw Exception("P")
}

