@file:Suppress("unused")

/**
 * This functions sums two integer numbers.
 *  >>> sum(5, 6) == 11
 */
fun sum(a: Int, b: Int) = a + b

/**
 * Meh, adds one two the sum
 *  >>> sum1(5, 6) == 12
 *  >>> sum1(0, 0) == 1
 */
fun sum1(a: Int, b: Int) = a + b + 1

class Const {
    companion object {
        /**
         *  >>> Const.constant() == 42
         */
        fun constant() = 43
    }
}

