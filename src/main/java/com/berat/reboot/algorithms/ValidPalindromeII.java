package com.berat.reboot.algorithms;

public class ValidPalindromeII {

    /**
     * Time Complexity:  O(n)
     * Space Complexity: O(1)
     *
     * Ends-to-middle two pointer.
     * When mismatch found, try skipping left or right character.
     * If either substring is a palindrome, return true.
     */
    public boolean validPalindrome(String s) {

        int left = 0, right = s.length() - 1;

        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                return isPalindrome(s, left + 1, right) || isPalindrome(s, left, right - 1);
            }
            left++;
            right--;
        }

        return true;
    }

    private boolean isPalindrome(String s, int left, int right) {
        while (left < right) {
            if (s.charAt(left) != s.charAt(right))
                return false;
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        ValidPalindromeII solution = new ValidPalindromeII();

        System.out.println(solution.validPalindrome("aba"));   // true
        System.out.println(solution.validPalindrome("abca"));  // true
        System.out.println(solution.validPalindrome("abc"));   // false
    }
}