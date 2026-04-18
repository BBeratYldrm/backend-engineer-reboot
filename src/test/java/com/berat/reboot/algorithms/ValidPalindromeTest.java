package com.berat.reboot.algorithms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidPalindromeTest {

    ValidPalindrome validPalindrome = new ValidPalindrome();

    @Test
    void validPalindrome_shouldReturnTrue(){

        String text = "A man a plan a canal Panama";

        assertTrue(validPalindrome.validPalindrome(text));

    }
}
