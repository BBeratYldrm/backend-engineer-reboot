package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class PermutationInString {

    /**
     * Time Complexity:  O(n) — single pass through s
     * Space Complexity: O(1) — maps hold at most 26 characters
     *
     * Build a frequency map for p.
     * Slide a fixed-size window over s, maintaining a window frequency map.
     * Add new char from right, remove old char from left when window exceeds p.length().
     * If windowMap equals pMap at any point, a permutation exists.
     */
    public boolean checkInclusion(String s, String p) {

        Map<Character, Integer> pMap = new HashMap<>();
        Map<Character, Integer> windowMap = new HashMap<>();

        for (char c : p.toCharArray()) {
            pMap.put(c, pMap.getOrDefault(c, 0) + 1);
        }

        for (int right = 0; right < s.length(); right++) {
            char rightChar = s.charAt(right);
            windowMap.put(rightChar, windowMap.getOrDefault(rightChar, 0) + 1);

            if (right >= p.length()) {
                char leftChar = s.charAt(right - p.length());
                windowMap.put(leftChar, windowMap.get(leftChar) - 1);
                if (windowMap.get(leftChar) == 0) windowMap.remove(leftChar);
            }

            if (windowMap.equals(pMap)) return true;
        }

        return false;
    }

    public static void main(String[] args) {
        PermutationInString solution = new PermutationInString();

        System.out.println(solution.checkInclusion("eidbaooo", "ab")); // true
        System.out.println(solution.checkInclusion("eidboaoo", "ab")); // false
        System.out.println(solution.checkInclusion("ab", "ab"));       // true
    }
}