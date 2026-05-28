package com.berat.reboot.algorithms;

public class IsSubsequence {

    /**
     * Time Complexity:  O(n) — single pass through t
     * Space Complexity: O(1) — two pointers, no extra space
     *
     * Two pointer — left tracks position in s, right scans t.
     * left advances only when s.charAt(left) matches t.charAt(right).
     * right always advances via the for loop.
     * If left reaches s.length(), all characters of s were found in order → true.
     * Short-circuit evaluation: left < s.length() must come before charAt to avoid index out of bounds.
     */
    public boolean isSubsequence(String s, String t) {

        if (s.length() == 0) {
            return true;
        }

        int left = 0;

        for (int right = 0; right < t.length(); right++) {
            if (left < s.length() && s.charAt(left) == t.charAt(right)) {
                left++;
            }
        }

        return left == s.length();
    }

    public static void main(String[] args) {
        IsSubsequence solution = new IsSubsequence();

        System.out.println(solution.isSubsequence("ace", "abcde")); // true
        System.out.println(solution.isSubsequence("axc", "ahbgdc")); // false
        System.out.println(solution.isSubsequence("", "ahbgdc"));   // true
    }
}