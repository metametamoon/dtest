package com.github.metametamoon.dtest.util


private const val kdocLineStarterLength = 3

fun List<String>.trimDocs(): List<String> = map { it.drop(kdocLineStarterLength) }

