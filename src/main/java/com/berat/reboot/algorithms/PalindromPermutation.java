package com.berat.reboot.algorithms;

import java.util.HashSet;
import java.util.Set;

public class PalindromPermutation {

    /**
     * Time Complexity:  O(n) — single pass through the string
     * Space Complexity: O(1) — set holds at most 26 characters
     *
     * Add each character to the set. If it's already there, remove it.
     * Characters seen an even number of times cancel out — set stays empty.
     * Characters seen an odd number of times remain in the set.
     * At the end, if set has 0 or 1 elements, a palindrome can be formed.
     */
    public boolean canMakePalindrome(String input) {

        Set<Character> set = new HashSet<>();

        for (char c : input.toCharArray()) {
            if (set.contains(c)) {
                set.remove(c);
            } else set.add(c);
        }
        return set.size() <= 1;
    }

    public static void main(String[] args) {
        PalindromPermutation solution = new PalindromPermutation();

        System.out.println(solution.canMakePalindrome("racecar")); // true
        System.out.println(solution.canMakePalindrome("aab"));     // true
        System.out.println(solution.canMakePalindrome("abc"));     // false
    }
}