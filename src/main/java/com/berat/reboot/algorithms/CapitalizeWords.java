package com.berat.reboot.algorithms;

public class CapitalizeWords {

    public String capitalizeWords(String input) {

        if (input == null || input.isBlank()) {
            return "";
        }
        String[] words = input.split(" ");

        String[] capitalizedWords = new String[words.length];

        for (int i = 0; i < words.length; i++) {

            String word = words[i];
            if (word.isEmpty()) {
                capitalizedWords[i] = "";
                continue;
            }

            char first = word.toUpperCase().charAt(0);
            String rest = word.substring(1).toLowerCase();
            capitalizedWords[i] = first + rest;

        }
        return String.join(" ", capitalizedWords);
    }
}
