package com.berat.reboot.algorithms;

import java.util.HashSet;
import java.util.Set;

public class MinSizeSubarraySum {

    public int minSubArrayLen(int s, int[] nums) {

        int left = 0;
        int currentSum = 0;
        int minLen = Integer.MAX_VALUE;

        for (int right = 0; right < nums.length; right++) {
            currentSum += nums[right];

            while (currentSum >= s) {
                minLen = Math.min(minLen, right - left + 1);
                currentSum -= nums[left];
                left++;
            }
        }
        return minLen == Integer.MAX_VALUE ? 0 : minLen;
    }

    public static void main(String[] args) {
        MinSizeSubarraySum solution = new MinSizeSubarraySum();

        System.out.println(solution.minSubArrayLen(7, new int[]{2, 3, 1, 2, 4, 3})); // 2
        System.out.println(solution.minSubArrayLen(4, new int[]{1, 4, 4}));           // 1
        System.out.println(solution.minSubArrayLen(11, new int[]{1, 1, 1, 1, 1, 1})); // 0
    }
}