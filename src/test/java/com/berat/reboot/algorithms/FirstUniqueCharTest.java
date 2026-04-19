package com.berat.reboot.algorithms;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FirstUniqueCharTest {

    FirstUniqueChar firstUniqueChar = new FirstUniqueChar();

    @ParameterizedTest
    @CsvSource({
            "leetcode , 0",
            "loveleetcode , 2",
            "aabb, -1 "
    })
    void firstUniqueChar_shouldReturnExpectedResult(String input, Integer result) {
        assertEquals(result, firstUniqueChar.firstUnique(input));
    }
}
