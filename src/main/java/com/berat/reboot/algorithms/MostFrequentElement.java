package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class MostFrequentElement {

    /**
     * Time Complexity:  O(n) — two passes through the array
     * Space Complexity: O(n) — map stores at most n distinct elements
     *
     * First pass: count how many times each number appears using a HashMap.
     * Second pass: iterate over the map entries to find the key with the highest value.
     * I track both max (the highest frequency seen so far) and result (the element that owns it).
     * When a higher frequency is found, I update both at the same time.
     */
    public int mostFrequent(int[] nums) {

        // count frequency of each element
        Map<Integer, Integer> map = new HashMap<>();
        for (int num : nums) map.put(num, map.getOrDefault(num, 0) + 1);

        // find the element with the highest frequency
        // entrySet() gives access to both key and value at the same time
        int max = 0;
        int result = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();       // update highest frequency
                result = entry.getKey();      // update the element that owns it
            }
        }
        return result;
    }

    public static void main(String[] args) {
        MostFrequentElement solution = new MostFrequentElement();

        System.out.println(solution.mostFrequent(new int[]{1, 2, 2, 3, 3, 3})); // 3
        System.out.println(solution.mostFrequent(new int[]{7, 7, 7, 1, 2}));    // 7
        System.out.println(solution.mostFrequent(new int[]{5}));                 // 5
    }
}