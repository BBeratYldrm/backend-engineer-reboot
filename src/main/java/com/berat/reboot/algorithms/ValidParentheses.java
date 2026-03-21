package com.berat.reboot.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;

public class ValidParentheses {

    public static boolean isValid(String s) { // I will take string and I will return true/false

        if (s == null || s.isEmpty()) return true;

        Deque<Character> stack = new ArrayDeque<>(); // I will keep the parentheses in here, but it should be in-line

        for (char c : s.toCharArray()) { // Look every char in string one by one
            if (c == '(' || c == '[' || c == '{') { // If i see any opening parentheses i will put in stack
                stack.push(c);
            } else { // If there is closed parentheses

                if (stack.isEmpty()) return false; // If i couldn't find any opening parentheses already

                char top = stack.pop(); // I need to get the last opened parenthesis

                if ((c == ')' && top != '(') || (c == ']' && top != '[') || (c == '}' && top != '{')) { // I need to check this closed parentheses, closing the correct parentheses ?
                    return false;
                }
            }
        }

        return stack.isEmpty(); // And i need to check, all parentheses are properly closed
    }
}
