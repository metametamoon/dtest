package com.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class `bar tests`() {
    @Test
    fun `0`() {
        assertEquals(3, bar().length)
    }

    @Test
    fun `1`() {
        assertTrue(bar().startsWith("b"))
    }
}