package com.khtn.trackme

import com.khtn.trackme.utils.Utils
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val number = 1.2182867396958998E-17
        print("number = ${Utils.formatDouble(number)}")
    }
}