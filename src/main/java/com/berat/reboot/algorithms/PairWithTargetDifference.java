package com.berat.reboot.algorithms;

import java.util.HashMap;
import java.util.Map;

public class PairWithTargetDifference {

    public int[] findPair(int[] nums, int target) {

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            int complement = target + nums[i];
            if (map.containsKey(complement)) {
                return new int[]{complement, nums[i]};
            }
            map.put(nums[i], i);

        }
        return new int[]{};
    }

    public static void main(String[] args) {
        PairWithTargetDifference solution = new PairWithTargetDifference();

        System.out.println(java.util.Arrays.toString(solution.findPair(new int[]{1, 5, 3, 4, 2}, 2))); // [1, 3] or [3, 5] or [2, 4]
        System.out.println(java.util.Arrays.toString(solution.findPair(new int[]{1, 2, 3}, 5)));        // []
    }
}