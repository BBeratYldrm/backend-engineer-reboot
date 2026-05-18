package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class CharacterFrequency {

    /**
     * Time Complexity:  O(n) — single pass through the string
     * Space Complexity: O(1) — map holds at most 26 characters (fixed alphabet)
     *
     * Convert string to char array, count each character using getOrDefault.
     * The map itself is the answer — no second pass needed.
     */
    public Map<Character, Integer> charFrequency(String input) {

        Map<Character, Integer> charFrequency = new HashMap<>();

        for (char c : input.toCharArray()) {
            charFrequency.put(c, charFrequency.getOrDefault(c, 0) + 1);
        }

        return charFrequency;
    }

    public static void main(String[] args) {
        CharacterFrequency solution = new CharacterFrequency();

        System.out.println(solution.charFrequency("hello"));   // {h=1, e=1, l=2, o=1}
        System.out.println(solution.charFrequency("aabbcc"));  // {a=2, b=2, c=2}
        System.out.println(solution.charFrequency("a"));       // {a=1}
    }
}