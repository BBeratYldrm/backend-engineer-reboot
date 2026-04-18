package com.berat.reboot.algorithms;

public class ValidPalindrome {

    public boolean validPalindrome(String input) {

        String cleaned = input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        StringBuilder builder = new StringBuilder(cleaned);
        builder.reverse();

        return cleaned.equals(builder.toString());
    }

    public static void main() {
        ValidPalindrome solution = new ValidPalindrome();

        System.out.println(solution.validPalindrome("A man a plan a canal Panama"));
    }
}
