package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

/**
 * Time Complexity:  O(n) — iterate both strings once
 * Space Complexity: O(1) — map holds at most 26 characters (fixed alphabet)
 **/
public class ValidAnagram {

    public boolean validAnagram(String inputA, String inputB) {

        // different lengths can't be anagrams
        if (inputA.length() == inputB.length()) {

            // key = character, value = how many times it appears
            Map<Character, Integer> letters = new HashMap<>();

            // count every character in inputA
            for (char c : inputA.toCharArray()) {
                letters.put(c, letters.getOrDefault(c, 0) + 1);
            }

            // now check inputB against our map
            for (char c : inputB.toCharArray()) {

                // inputA didn't have this character at all → not an anagram
                if (!letters.containsKey(c)) return false;

                // character exists → decrease its count
                letters.put(c, letters.get(c) - 1);

                // count went below 0 → inputB has more of this character than inputA → not an anagram
                if (letters.get(c) < 0) return false;
            }

            // all characters matched perfectly
            return true;
        }

        return false;
    }
}
