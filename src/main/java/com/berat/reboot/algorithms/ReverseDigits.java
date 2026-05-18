package com.berat.reboot.algorithms;

import java.util.ArrayDeque;
import java.util.Deque;

public class ReverseDigits {

    /**
     * Time Complexity:  O(n) — two passes through the string
     * Space Complexity: O(n) — stack holds at most n digits
     *
     * First pass: push all digits onto the stack.
     * Second pass: rebuild the string — non-digits stay in place,
     * digits get replaced with stack.pop() which gives them in reverse order.
     * Stack's LIFO behavior does the reversal automatically.
     */
    public String reverseDigits(String input) {

        // collect digits in order — stack will reverse them on pop
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) stack.push(c);
        }

        // rebuild string — swap digits with reversed ones, leave rest unchanged
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c)) {
                sb.append(c);
            } else {
                sb.append(stack.pop());
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        ReverseDigits solution = new ReverseDigits();

        System.out.println(solution.reverseDigits("a1b2c3")); // a3b2c1
        System.out.println(solution.reverseDigits("h3ll0"));  // h0ll3
        System.out.println(solution.reverseDigits("abc"));    // abc
    }
}