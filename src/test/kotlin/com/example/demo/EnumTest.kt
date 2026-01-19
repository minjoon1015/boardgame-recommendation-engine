package com.example.demo

import com.example.demo.enum.BoardGameCategory
import org.junit.jupiter.api.Test

class EnumTest {
    @Test
    fun test() {
        println(BoardGameCategory.entries)
    }
}