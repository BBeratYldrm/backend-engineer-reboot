package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class FirstUniqueChar {

    /**
     * Time Complexity:  O(n) — two passes through the string
     * Space Complexity: O(1) — map holds at most 26 characters (fixed alphabet)
     */
    public int firstUnique(String input) {

        // count how many times each character appears
        Map<Character, Integer> charMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            charMap.put(c, charMap.getOrDefault(c, 0) + 1);
        }

        // find the first character that appears exactly once
        // we go through the original string to preserve order
        for (int i = 0; i < input.length(); i++) {
            if (charMap.get(input.charAt(i)) == 1) {
                return i;
            }
        }

        // no unique character found
        return -1;
    }
}