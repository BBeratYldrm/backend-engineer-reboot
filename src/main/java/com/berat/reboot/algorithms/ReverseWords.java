package com.berat.reboot.algorithms;

import java.util.Arrays;
import java.util.Collections;

public class ReverseWords {

    /**
     * Time Complexity:  O(n) — trim, split, and loop all go through the string once
     * Space Complexity: O(n) — storing the split words array and the result
     */
    public String reverseWords(String input) {

        // remove leading and trailing spaces first
        String[] words = input.trim().split(" ");

        // StringBuilder is more efficient than String concatenation in a loop
        // String is immutable — every concat creates a new object
        // StringBuilder reuses the same object
        StringBuilder result = new StringBuilder();

        // iterate from the last word to the first
        for (int i = words.length - 1; i >= 0; i--) {
            result.append(words[i]).append(" ");
        }

        // trim the trailing space we added in the loop
        return result.toString().trim();
    }

    public String reverseWordsAlternative(String input) {
        String[] words = input.trim().split("\\s+");
        Collections.reverse(Arrays.asList(words));
        return String.join(" ", words);
    }

    public static void main(String[] args) {
        ReverseWords reverseWords = new ReverseWords();

        System.out.println(reverseWords.reverseWords("the sky is blue"));
        System.out.println(reverseWords.reverseWords("  hello world  "));
    }
}