package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class TwoSum {

    /**
     * Time Complexity:  O(n) — single pass through the array
     * Space Complexity: O(n) — map stores at most n elements
     */
    public int[] twoSum(int[] nums, int targetNum) {

        // key = number, value = its index
        Map<Integer, Integer> twoSumMap = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {

            // what number do we need to reach the target?
            int difference = targetNum - nums[i];

            // if we've seen that number before → we found our pair
            if (twoSumMap.containsKey(difference)) {
                return new int[]{twoSumMap.get(difference), i};
            }

            // haven't found a pair yet → store current number and its index
            twoSumMap.put(nums[i], i);
        }

        throw new IllegalArgumentException("No valid solution found.");
    }
}