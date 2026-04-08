package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class TwoSum {

    public int[] twoSum(int[] nums, int targetNum) {

        // Key: number we've seen, Value: its index
        // We store numbers as we go, so we can look up complements
        Map<Integer, Integer> twoSumMap = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {

            // If nums[i] + complement = target, then complement = target - nums[i]
            // Example: target=9, nums[i]=2 → complement=7
            int complement = targetNum - nums[i];

            // Did we see the complement before?
            // If yes → we found our pair
            if (twoSumMap.containsKey(complement)) {
                return new int[]{twoSumMap.get(complement), i};
            }

            // Complement not found yet → store current number with its index
            // Future elements might need this number as their complement
            twoSumMap.put(nums[i], i);
        }

        // Problem guarantees a solution exists, but good practice to handle this
        throw new IllegalArgumentException("No solution found");
    }

    public static void main(String[] args) {
        TwoSum solution = new TwoSum();

        // Test 1: 2 + 7 = 9 → indices [0, 1]
        int[] result = solution.twoSum(new int[]{2, 7, 11, 15}, 9);
        System.out.println(result[0] + ", " + result[1]); // expected: 0, 1

        // Test 2: 2 + 4 = 6 → indices [1, 2]
        int[] result2 = solution.twoSum(new int[]{3, 2, 4}, 6);
        System.out.println(result2[0] + ", " + result2[1]); // expected: 1, 2
    }
}