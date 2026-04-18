package com.berat.reboot.algorithms;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidAnagramTest {

    ValidAnagram validAnagram = new ValidAnagram();

    @ParameterizedTest
    @CsvSource({
            "listen, silent, true",
            "hello,  world,  false",
            "rat,    car,    false",
            "anagram, nagaram, true"
    })
    void validAnagram_shouldReturnExpectedResult(String inputA, String inputB, boolean expected) {
        assertEquals(expected, validAnagram.validAnagram(inputA, inputB));
    }
}