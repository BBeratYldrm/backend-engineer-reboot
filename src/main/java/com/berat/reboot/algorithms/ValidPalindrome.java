package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class ValidPalindrome {

    /**
     * Approach 1 — StringBuilder reverse
     * Time Complexity:  O(n) — replaceAll + reverse + equals, all linear
     * Space Complexity: O(n) — cleaned string + StringBuilder copy
     */
    public boolean validPalindrome(String input) {

        // remove non-alphanumeric characters and lowercase
        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        StringBuilder builder = new StringBuilder(cleaned);
        builder.reverse();

        return cleaned.equals(builder.toString());
    }

    /**
     * Approach 2 — Two Pointer O(1) space
     * Time Complexity:  O(n) — single pass through the string
     * Space Complexity: O(1) — no cleaned string, pointers only
     *
     * Skip non-alphanumeric characters with inner while loops.
     * Compare characters case-insensitively with toLowerCase.
     * Mismatch found → not a palindrome. All matched → palindrome.
     */
    public boolean validPalindromeOptimized(String input) {

        int left = 0;
        int right = input.length() - 1;

        // move pointers toward each other
        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(input.charAt(left))) left++;
            while (left < right && !Character.isLetterOrDigit(input.charAt(right))) right--;

            if (Character.toLowerCase(input.charAt(left)) != Character.toLowerCase(input.charAt(right))) {
                return false;
            }
            left++;
            right--;
        }

        // all characters matched
        return true;
    }

    public static void main(String[] args) {
        ValidPalindrome solution = new ValidPalindrome();

        System.out.println(solution.validPalindrome("A man a plan a canal Panama")); // true
        System.out.println(solution.validPalindrome("race a car"));                   // false
        System.out.println(solution.validPalindromeOptimized("racecar"));             // true
        System.out.println(solution.validPalindromeOptimized("hello"));               // false
    }
}